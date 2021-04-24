package com.kangdroid.navi_arch.pager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kangdroid.navi_arch.databinding.RecyclerFileListViewBinding
import com.kangdroid.navi_arch.recyclerview.FileAdapter

class PagerAdapter : RecyclerView.Adapter<PagerAdapter.PagerViewHolder>() {

    var pageList: List<FileAdapter> = mutableListOf()

    inner class PagerViewHolder(private val recyclerFileListViewBinding: RecyclerFileListViewBinding) :
        RecyclerView.ViewHolder(recyclerFileListViewBinding.root) {

        fun bind(fileAdapter: FileAdapter) {
            recyclerFileListViewBinding.naviMainRecycler.adapter = fileAdapter
            recyclerFileListViewBinding.naviMainRecycler.layoutManager = LinearLayoutManager(
                recyclerFileListViewBinding.root.context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val recyclerFileListViewBinding: RecyclerFileListViewBinding =
            RecyclerFileListViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

        return PagerViewHolder(recyclerFileListViewBinding)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bind(pageList[position])
    }

    override fun getItemCount(): Int = pageList.size

    fun setNaviPageList(pageList: List<FileAdapter>) {
        this.pageList = pageList
        notifyDataSetChanged()
    }
}