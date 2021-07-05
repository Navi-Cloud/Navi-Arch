package com.kangdroid.navi_arch.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.databinding.DialogAddBinding
import com.kangdroid.navi_arch.viewmodel.MenuBottomSheetViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MenuBottomSheetFragment @Inject constructor() : BottomSheetDialogFragment() {
    private val logTag: String = this::class.java.simpleName
    private var _dialogAddBinding: DialogAddBinding? = null
    private val dialogAddBinding: DialogAddBinding get() = _dialogAddBinding!!
    private val menuBottomSheetViewModel: MenuBottomSheetViewModel by viewModels()

    var currentFolderToken: String = ""
        set(value) {
            Log.d(logTag, "Setting currentFolderToken as $value")
            field = value
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

        dialogAddBinding.makeFolderLayout.setOnClickListener {
            val folderNameDialog: FolderNameDialog = FolderNameDialog(
                context = requireContext(),
                createFolderLogic = { fileName ->
                    menuBottomSheetViewModel.createFolder(
                        createFolderRequestDTO = CreateFolderRequestDTO(
                            parentFolderToken = currentFolderToken,
                            newFolderName = fileName
                        ),
                        onSuccess = {
                            Log.d(logTag, "Successfully created folder $it")
                            // Probably need to refresh
                        },
                        onFailure = {
                            Toast.makeText(requireContext(), "Cannot create folder: ${it.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    )
                }
            )
            folderNameDialog.show()
        }
    }

    override fun onDestroyView() {
        _dialogAddBinding = null
        super.onDestroyView()
    }
}