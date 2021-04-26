package com.kangdroid.navi_arch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.pager.FileSortingMode
import com.kangdroid.navi_arch.pager.PagerAdapter
import com.kangdroid.navi_arch.pager.PagerViewModel
import com.kangdroid.navi_arch.recyclerview.FileAdapter
import com.kangdroid.navi_arch.uploading.UploadingViewModel

// The View
class MainActivity : AppCompatActivity() {

    // View Binding
    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // Pager Adapter for ViewPager2
    private val pageAdapter: PagerAdapter by lazy {
        PagerAdapter()
    }

    // Pager ViewModel
    private val pagerViewModel: PagerViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(PagerViewModel::class.java)
    }

    // Uploading ViewModel - Since we are NOT sharing some data FOR NOW, but
    // in case of code growing for uploading, leave it as View Model
    private val uploadingViewModel: UploadingViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(UploadingViewModel::class.java)
    }

    // Value for detecting this activity launched with normal activity or Upload Activity.
    private val uploadingIdentifier: String = "UPLOAD_ACTIVITY"
    private val uploadingEnabled: String = "UPLOADING_ENABLED"
    private val getContentRequestCode: Int = 10
    private val getUploadingActivityRequestCode: Int = 20
    private var isUploadingEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // Check whether this is launched with uploadingIdentifier.
        isUploadingEnabled = (intent.getStringExtra(uploadingIdentifier) == uploadingEnabled)

        // If uploading is enabled, first select file to choose!
        if (isUploadingEnabled) {
            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            startActivityForResult(intent, getContentRequestCode)
        }

        // Init Pager Adapter
        initPager()

        // Init View Model
        initViewModel()

        // Init toggle button
        initToggleButton()

        // Now start from initial page
        pagerViewModel.createInitialRootPage()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isUploadingEnabled) {
            // Enable Folder Check Button when launched with UploadActivity
            menuInflater.inflate(R.menu.uploading_action, menu)
        } else {
            // Enable Uploading Button when launched with MainActivity
            menuInflater.inflate(R.menu.main_action, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Upload button when launched with MainActivity
            R.id.action_upload -> {
                val intent: Intent = Intent(this, MainActivity::class.java).apply {
                    putExtra(uploadingIdentifier, uploadingEnabled)
                }
                startActivityForResult(intent, getUploadingActivityRequestCode)
                true
            }

            // Check[Folder Check Button] when launched with UploadActivity
            R.id.action_select_path -> {
                Log.d(this::class.java.simpleName, "Uploading Folder path selected.")
                // Upload it!
                val currentPageList: MutableList<FileAdapter> = pagerViewModel.livePagerData.value!!
                uploadingViewModel.upload(currentPageList[activityMainBinding.viewPager.currentItem].currentFolder.token) {
                    // Set UploadingActivity Result as RESULT_OK
                    val intent: Intent = Intent().apply {
                        setResult(RESULT_OK)
                    }

                    // Finish Activity
                    finish()
                }
                true
            }
            else -> false
        }
    }

    // For UploadActivity[For getting upload target file]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == getContentRequestCode && resultCode == RESULT_OK) {
            data?.data?.also {
                uploadingViewModel.createFileUri(it)
            }
        } else if (requestCode == getUploadingActivityRequestCode && resultCode == RESULT_OK) {
            // Update view since file is uploaded
            val currentPageList: MutableList<FileAdapter> = pagerViewModel.livePagerData.value!!
            pagerViewModel.explorePage(
                currentPageList[activityMainBinding.viewPager.currentItem].currentFolder,
                activityMainBinding.viewPager.currentItem,
                true
            )
        }
    }

    private fun initPager() {
        activityMainBinding.viewPager.adapter = pageAdapter

        TabLayoutMediator(
            activityMainBinding.mainTab,
            activityMainBinding.viewPager
        ) { tab, position ->
            tab.text = if (position == 0) {
                "/"
            } else {
                pagerViewModel.livePagerData.value?.get(position)?.currentFolder?.getBriefName()
            }
        }.attach()
    }

    private fun initViewModel() {
        // Set Data Observer for pageData
        pagerViewModel.livePagerData.observe(this, Observer<List<FileAdapter>> {
            Log.d(this::class.java.simpleName, "Observed, Setting changed page")
            pageAdapter.setNaviPageList(it)
            activityMainBinding.viewPager.currentItem = it.lastIndex
        })
    }

    private fun initToggleButton() {
        // Toggle Buttons[Sorting Buttons]
        val sortListener: (CompoundButton, Boolean) -> Unit = { _, _ ->
            pagerViewModel.sort(
                when (activityMainBinding.sortByNameOrLMT.isChecked) {
                    true -> if (activityMainBinding.sortByType.isChecked) FileSortingMode.LMT else FileSortingMode.TypedLMT
                    false -> if (activityMainBinding.sortByType.isChecked) FileSortingMode.Name else FileSortingMode.TypedName
                },
                activityMainBinding.sortByAscendingOrDescending.isChecked,
                activityMainBinding.viewPager.currentItem
            )
        }

        // Toggle Button INIT
        with(activityMainBinding) {
            sortByNameOrLMT.setOnCheckedChangeListener(sortListener)
            sortByType.setOnCheckedChangeListener(sortListener)
            sortByAscendingOrDescending.setOnCheckedChangeListener(sortListener)
        }
    }
}