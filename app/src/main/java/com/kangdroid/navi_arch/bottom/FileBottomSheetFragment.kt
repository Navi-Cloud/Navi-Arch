package com.kangdroid.navi_arch.bottom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.databinding.LayoutBottomBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class FileBottomSheetFragment @Inject constructor(): BottomSheetDialogFragment() {
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
            it.bottomFileDownload.text = resources.getString(R.string.bottom_sheet_default_download, targetFileData?.fileName)

            // Set On Click Listener for download
            targetFileData?.let { inputFileData ->
                // TODO: Check Storage Permission First
                it.bottomFileDownloadView.setOnClickListener { _ ->
                    fileBottomSheetViewModel.downloadFile(inputFileData.token)
                }
            }
        }
    }
}