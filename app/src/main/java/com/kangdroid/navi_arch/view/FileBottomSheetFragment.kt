package com.kangdroid.navi_arch.view

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.databinding.LayoutBottomBinding
import com.kangdroid.navi_arch.viewmodel.FileBottomSheetViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FileBottomSheetFragment @Inject constructor(): BottomSheetDialogFragment() {
    // Log Tag
    private val logTag: String = this::class.java.simpleName

    // Permission Request
    private var permissionGranted: Boolean = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    // The target file data[For showing the information of file]
    var targetFileData: FileData? = null

    // Layout Binding
    private var layoutBottomBinding: LayoutBottomBinding? = null

    // View Model for File Bottom Sheet[DI]
    private val fileBottomSheetViewModel: FileBottomSheetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Add Launcher for Requesting Permissions
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissionGranted =
                (it[Manifest.permission.READ_EXTERNAL_STORAGE] == true).or(it[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true)

            if (!permissionGranted) {
                Toast.makeText(context, R.string.file_storage_permission_denied, Toast.LENGTH_LONG)
                    .show()
            }
        }

        // Layout Inflate
        layoutBottomBinding = LayoutBottomBinding.inflate(layoutInflater, container, false)
        return layoutBottomBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBottomBinding?.let {
            // Set Image File/Folder
            it.bottomFileType.setImageResource(
                when (targetFileData?.fileType) {
                    FileType.Folder.toString() -> R.drawable.ic_common_folder_24
                    FileType.File.toString() -> R.drawable.ic_common_file_24
                    else -> R.drawable.ic_common_file_24
                }
            )

            // Set Corresponding texts
            it.bottomFileName.text = targetFileData?.fileName
            if (targetFileData?.fileType == FileType.Folder.toString()) {
                // Disable Download when folder is long-clicked
                it.bottomFileDownloadView.visibility = View.GONE
            } else {
                it.bottomFileDownload.text = resources.getString(R.string.bottom_sheet_default_download, targetFileData?.fileName)

                // Set On Click Listener for download
                targetFileData?.let { inputFileData ->
                    // TODO: Check Storage Permission First
                    it.bottomFileDownloadView.setOnClickListener { _ ->
                        checkPermission()
                        if (permissionGranted) {
                            fileBottomSheetViewModel.downloadFile(inputFileData.token)
                            this.dismiss() // dismiss this fragment after download completed.
                        }
                    }
                }
            }
        }
    }

    private fun checkPermission() {
        val isPermissionGrantedInternal: Int = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE).or(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE))

        if (isPermissionGrantedInternal == PackageManager.PERMISSION_DENIED) {
            Log.d(logTag, "READ Permission is denied. Requesting permissions.")
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        } else if (isPermissionGrantedInternal == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
        }
    }
}