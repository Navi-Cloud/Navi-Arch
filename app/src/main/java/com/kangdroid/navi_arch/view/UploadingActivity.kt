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
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.request.FileCopyRequest
import com.kangdroid.navi_arch.viewmodel.UploadingViewModel

class UploadingActivity: PagerActivity() {
    // Uploading ViewModel - Since we are NOT sharing some data FOR NOW, but
    // in case of code growing for uploading, leave it as View Model
    private val uploadingViewModel: UploadingViewModel by viewModels()
    private var filecopyrequest : FileCopyRequest = FileCopyRequest("","","","")
    private var isMoveFile : Boolean = false

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

        //checking whether it's from MenuBottom or FileBottom
        // isMoveFile is true -> FileBottom
        // false -> MenuBottom
        isMoveFile = intent.getBooleanExtra("ismove",false)
        if(!isMoveFile) getContentActivity()
        else checkFileCopyRequest()
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
                val currentPageList: MutableList<FileAdapter> = pagerViewModel.livePagerData.value!!
                // Upload it!
                if(!isMoveFile){
                    Log.d(this::class.java.simpleName, "Uploading Folder path selected.")
                    uploadingViewModel.upload(currentPageList[activityMainBinding.viewPager.currentItem].currentFolder.token)
                }
                else {
                    Log.d(this::class.java.simpleName, "Moving Folder path selected.")
                    filecopyrequest.toPrevToken = currentPageList[activityMainBinding.viewPager.currentItem].currentFolder.token
                    uploadingViewModel.move(filecopyrequest)
                }
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
    }

    private fun checkFileCopyRequest(){
        val filedata : FileData = intent.getSerializableExtra("filedata") as FileData
        filecopyrequest.fromToken = filedata.token
        filecopyrequest.fromPrevToken = filedata.prevToken
        filecopyrequest.newFileName = filedata.fileName
    }
}