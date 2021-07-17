package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.databinding.DialogAddBinding
import com.kangdroid.navi_arch.viewmodel.MenuBottomSheetViewModel

class MenuBottomSheetFragment(
    val currentFolderToken: String,
    val refreshPage: () -> Unit
) : BottomSheetDialogFragment() {
    private val logTag: String = this::class.java.simpleName
    private var _dialogAddBinding: DialogAddBinding? = null
    private val dialogAddBinding: DialogAddBinding get() = _dialogAddBinding!!
    private val menuBottomSheetViewModel: MenuBottomSheetViewModel by viewModels()

    // UploadingActivity Results Callback
    private val afterUploadingActivityFinishes: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                // Update view since file is uploaded
                refreshPage()
                dismiss()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _dialogAddBinding = DialogAddBinding.inflate(inflater, container, false)
        return dialogAddBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        menuBottomSheetViewModel.createFolderResult.observe(viewLifecycleOwner) {
            if (it.isSucceed) {
                Log.d(logTag, "Successfully created folder!")
                // Probably need to refresh
                refreshPage()
            } else {
                Log.e(logTag, "Cannot create folder: ${it.error?.message}")
                Toast.makeText(requireContext(), "Cannot create folder: ${it.error?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        dialogAddBinding.makeFolderLayout.setOnClickListener {
            val folderNameDialog: FolderNameDialog = FolderNameDialog(
                createFolderLogic = { fileName ->
                    menuBottomSheetViewModel.createFolder(
                        createFolderRequestDTO = CreateFolderRequestDTO(
                            parentFolderToken = currentFolderToken,
                            newFolderName = fileName
                        )
                    )
                }
            )
            folderNameDialog.show(parentFragmentManager, "")
        }

        dialogAddBinding.uploadLayout.setOnClickListener {
            afterUploadingActivityFinishes.launch(
                Intent(requireContext(), UploadingActivity::class.java)
            )
        }
    }

    override fun onDestroyView() {
        _dialogAddBinding = null
        super.onDestroyView()
    }
}