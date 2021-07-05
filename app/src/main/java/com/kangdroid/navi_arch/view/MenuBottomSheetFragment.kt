package com.kangdroid.navi_arch.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kangdroid.navi_arch.databinding.DialogAddBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MenuBottomSheetFragment @Inject constructor() : BottomSheetDialogFragment() {
    private var _dialogAddBinding: DialogAddBinding? = null
    private val dialogAddBinding: DialogAddBinding get() = _dialogAddBinding!!

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
            val folderNameDialog: FolderNameDialog = FolderNameDialog(requireContext())
            folderNameDialog.show()
        }
    }

    override fun onDestroyView() {
        _dialogAddBinding = null
        super.onDestroyView()
    }
}