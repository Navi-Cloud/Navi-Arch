package com.kangdroid.navi_arch.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.adapter.PagerAdapter
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.PagerViewModel
import com.kangdroid.navi_arch.viewmodel.UploadingViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.HttpUrl
import javax.inject.Inject

// The View
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // View Binding
    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // Pager ViewModel
    private val pagerViewModel: PagerViewModel by viewModels()

    // Pager Adapter[DI]
    @Inject
    lateinit var pageAdapter: PagerAdapter

    // BottomSheetDialog[DI]
    @Inject
    lateinit var bottomSheetFragment: FileBottomSheetFragment

    // Uploading ViewModel - Since we are NOT sharing some data FOR NOW, but
    // in case of code growing for uploading, leave it as View Model
    private val uploadingViewModel: UploadingViewModel by viewModels()

    // Value for detecting this activity launched with normal activity or Upload Activity.
    private val uploadingIdentifier: String = "UPLOAD_ACTIVITY"
    private val uploadingEnabled: String = "UPLOADING_ENABLED"
    private val getContentRequestCode: Int = 10
    private val getUploadingActivityRequestCode: Int = 20
    private var isUploadingEnabled: Boolean = false

    // Menu for dynamically hide - show
    private lateinit var dynamicMenu: Menu

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
            dynamicMenu = menu!!
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
            R.id.action_add_folder -> {
                val builder = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.dialog_add, null)
                //button event
                val btn_make_folder = dialogView.findViewById<LinearLayout>(R.id.layout_make_folder)
                btn_make_folder.setOnClickListener {
                    val builder2 = AlertDialog.Builder(this)
                    val dialogView2 = layoutInflater.inflate(R.layout.dialog_add_folder, null)
                    builder2.setView(dialogView2)
                        .setPositiveButton("확인"){
                            _, _ ->
                        }
                        .setNegativeButton("취소"){
                            _, _ ->
                        }
                        .show()
                }

                builder.setView(dialogView)
                val alertDialog = builder.create()
                val window = alertDialog.window
                window?.setGravity(Gravity.BOTTOM)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
                alertDialog.show()
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
        // Set onLongClickListener to viewModel
        pagerViewModel.recyclerOnLongClickListener = {
            bottomSheetFragment.targetFileData = it
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            true
        }

        // Set Data Observer for pageData
        pagerViewModel.livePagerData.observe(this, Observer<List<FileAdapter>> {
            Log.d(this::class.java.simpleName, "Observed, Setting changed page")
            pageAdapter.setNaviPageList(it)
            activityMainBinding.viewPager.currentItem = it.lastIndex
        })

        // Set Error Message Observer
        pagerViewModel.liveErrorData.observe(this) {
            Log.e(this::class.java.simpleName, "Error Message Observed")
            Log.e(this::class.java.simpleName, it.stackTraceToString())
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            dynamicMenu.findItem(R.id.action_upload).isVisible = false
        }
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