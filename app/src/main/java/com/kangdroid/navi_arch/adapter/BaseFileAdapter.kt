package com.kangdroid.navi_arch.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.databinding.ItemFileBinding

open class BaseFileAdapter (
    val onClick: (FileData, Int) -> Unit,
    var fileList: List<FileData>,
) :
    RecyclerView.Adapter<BaseFileAdapter.FileViewHolder>() {

    open fun setOnClickListener(root: LinearLayout, fileData: FileData) {
        root.setOnClickListener {
            onClick(fileData, 0)
        }
    }

    open inner class FileViewHolder(private val itemFileBinding: ItemFileBinding) :
        RecyclerView.ViewHolder(itemFileBinding.root) {

        // Bind fileData to object
        open fun bind(fileData: FileData) {
            itemFileBinding.fileName.text = fileData.getBriefName()
            itemFileBinding.lastModifiedTime.text = fileData.getFormattedDate()
            itemFileBinding.imgFileType.setImageResource(
                when (fileData.fileType) {
                    FileType.File.toString() -> R.drawable.ic_common_file_24
                    FileType.Folder.toString() -> R.drawable.ic_common_folder_24
                    else -> R.drawable.ic_common_file_24
                }
            )

            // Set on click event
            setOnClickListener(itemFileBinding.root, fileData)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val itemFileBinding: ItemFileBinding =
            ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(itemFileBinding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileList[position])
    }

    override fun getItemCount(): Int = fileList.size

    fun setBaseFileList(fileList: List<FileData>) {
        this.fileList = fileList
        notifyDataSetChanged()
    }
}