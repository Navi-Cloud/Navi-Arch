package com.kangdroid.navi_arch.adapter

import android.widget.LinearLayout
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.databinding.ItemFileBinding

class FileAdapter(
    onClick: (FileData, Int) -> Unit,
    fileList: List<FileData>,
    val onLongClick: ((FileData) -> Boolean)?,
    val pageNumber: Int,
    val currentFolder: FileData
) : BaseFileAdapter(onClick, fileList) {

    override fun setOnClickListener(root: LinearLayout, fileData: FileData) {
        // on-clicked
        root.setOnClickListener {
            onClick(fileData, pageNumber)
        }

        // When each row long-clicked
        root.setOnLongClickListener {
            onLongClick?.invoke(fileData) ?: false
        }
    }
}