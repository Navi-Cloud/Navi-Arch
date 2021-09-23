package com.kangdroid.navi_arch.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.adapter.PagerAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.databinding.RecyclerFileListViewBinding
import com.kangdroid.navi_arch.viewmodel.PagerViewModel

class MyfileFragment : Fragment() {

    private lateinit var myfilefragmentBinding : RecyclerFileListViewBinding

    // Pager ViewModel
    protected val pagerViewModel: PagerViewModel by viewModels()

    // Pager Adapter[DI]
    private val pageAdapter: PagerAdapter = PagerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myfilefragmentBinding = RecyclerFileListViewBinding.inflate(layoutInflater, container, false)
        return myfilefragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myfilefragmentBinding.apply {
            //attach adapter
            naviMainRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            naviMainRecycler.adapter = pageAdapter
        }
    }
}