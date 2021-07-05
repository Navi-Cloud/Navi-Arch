package com.kangdroid.navi_arch.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.kangdroid.navi_arch.databinding.DialogAddFolderBinding

class FolderNameDialog(context: Context) : Dialog(context) {
    private val logTag: String = this::class.java.simpleName
    private val dialogAddFolderBinding: DialogAddFolderBinding by lazy {
        DialogAddFolderBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(dialogAddFolderBinding.root)

        dialogAddFolderBinding.cancelCreateFolder.setOnClickListener {
            Log.d(logTag, "Cancel Clicked!")
            dismiss()
        }

        dialogAddFolderBinding.confirmCreateFolder.setOnClickListener {
            Log.d(logTag, "Confirm Clicked!")
            dismiss()
        }
    }
}