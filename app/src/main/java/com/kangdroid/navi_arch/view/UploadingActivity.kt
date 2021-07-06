package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.viewmodel.UploadingViewModel

class UploadingActivity: PagerActivity() {
    // Uploading ViewModel - Since we are NOT sharing some data FOR NOW, but
    // in case of code growing for uploading, leave it as View Model
    private val uploadingViewModel: UploadingViewModel by viewModels()

    private val getContentRequestCode: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == getContentRequestCode && resultCode == RESULT_OK) {
            data?.data?.also {
                uploadingViewModel.createFileUri(it)
            }
        }
    }

    private fun getContentActivity() {
        // Call for getting content itself
        val intent: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
        }
        startActivityForResult(intent, getContentRequestCode)
    }
}