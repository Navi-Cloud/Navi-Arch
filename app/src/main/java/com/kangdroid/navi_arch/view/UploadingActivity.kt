package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.viewmodel.UploadingViewModel

class UploadingActivity: PagerActivity() {
    // Uploading ViewModel - Since we are NOT sharing some data FOR NOW, but
    // in case of code growing for uploading, leave it as View Model
    private val uploadingViewModel: UploadingViewModel by viewModels()
//    lateinit var currentPageList: MutableList<FileAdapter>

    // Result Callback[StartActivityForResult]
    private val resultCallbackLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            it.data?.data?.also { finalUri ->
                uploadingViewModel.createFileUri(finalUri)
            }
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpObserver()
        getContentActivity()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Enable Folder Check Button when launched with UploadActivity
        menuInflater.inflate(R.menu.uploading_action, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            // Check[Folder Check Button] when launched with UploadActivity
            R.id.action_select_path -> {
                Log.d(this::class.java.simpleName, "Uploading Folder path selected.")
                // Upload it!
                pagerViewModel.livePagerData.observe(this){
                    uploadingViewModel.upload(it[activityMainBinding.viewPager.currentItem].currentFolder.token)
                }
//                val currentPageList: MutableList<FileAdapter> = pagerViewModel.livePagerData.value!!
//                uploadingViewModel.upload(currentPageList[activityMainBinding.viewPager.currentItem].currentFolder.token)

                true
            }
            else -> false
        }
    }

    private fun getContentActivity() {
        // Call for getting content itself
        resultCallbackLauncher.launch(
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
        )
    }

    private fun setUpObserver() {
        uploadingViewModel.fileUploadSucceed.observe(this) {
            if (it) {
                // Set UploadingActivity Result as RESULT_OK
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Failed upload file! Try again in few minutes!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
//        pagerViewModel.livePagerData.observe(this){
//            currentPageList = it
//        }
    }
}