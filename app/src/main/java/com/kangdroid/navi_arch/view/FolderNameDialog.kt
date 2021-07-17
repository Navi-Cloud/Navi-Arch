package com.kangdroid.navi_arch.view

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kangdroid.navi_arch.databinding.DialogAddFolderBinding

class FolderNameDialog(private val createFolderLogic: (String) -> Unit) : DialogFragment() {
    private val logTag: String = this::class.java.simpleName
    private var _dialogBinding: DialogAddFolderBinding? = null
    private val dialogAddFolderBinding: DialogAddFolderBinding get() = _dialogBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _dialogBinding = DialogAddFolderBinding.inflate(inflater, container, false)
        return dialogAddFolderBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogAddFolderBinding.cancelCreateFolder.setOnClickListener {
            Log.d(logTag, "Cancel Clicked!")
            dismiss()
        }

        dialogAddFolderBinding.confirmCreateFolder.setOnClickListener {
            Log.d(logTag, "Confirm Clicked!")
            createFolderLogic(dialogAddFolderBinding.folderName.text.toString())
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _dialogBinding = null
    }
}