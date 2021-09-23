package com.kangdroid.navi_arch.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kangdroid.navi_arch.adapter.BaseFileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.databinding.ActivitySearchBinding
import com.kangdroid.navi_arch.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    // Log Tag
    private val logTag: String = this::class.java.simpleName

    // View binding
    private val searchBinding: ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(layoutInflater)
    }

    // Search ViewModel
    private val searchViewModel: SearchViewModel by viewModels()

    // For Sorting
    private var currentSortMode: FileSortingMode = FileSortingMode.TypedName
    private var isReversed: Boolean = false

    private val recyclerOnClickListener: ((FileData, Int) -> Unit) = { fileData, _ ->
        // Go to MainActivity for show file list of selected folder
        val intent = Intent(this, MyfileActivity::class.java)
        intent.putExtra("searchFolder", fileData)
        startActivity(intent)
    }

    private var baseFileAdapter: BaseFileAdapter = BaseFileAdapter(
        onClick = recyclerOnClickListener,
        fileList = listOf()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(searchBinding.root)

        initBinding()

        initObserver()
    }

    private fun initBinding() {
        searchBinding.apply {
            // Search Edit Text
            searchBar.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_ENTER){
                    // Show ProgressBar
                    progressBar.visibility = View.VISIBLE

                    // Search file
                    val query: String = mainHint.text.toString()
                    Log.d(logTag, "$query!")
                    searchViewModel.search(
                        query = query,
                        mode = currentSortMode,
                        isReversed = isReversed
                    )
                    true
                } else false
            }

            // Recycler View
            searchResultRecyclerView.layoutManager =
                LinearLayoutManager(this@SearchActivity, LinearLayoutManager.VERTICAL, false)
            searchResultRecyclerView.adapter = baseFileAdapter

        }
    }

    private fun initObserver() {
        searchViewModel.searchResultLiveData.observe(this) {
            // Hide ProgressBar first
            searchBinding.progressBar.visibility = View.GONE

            // Handle search result
            if (it != null) {
                Log.d(logTag, "Search success!: ${it.size}")
                if(it.isEmpty()){
                    searchBinding.searchResultRecyclerView.visibility = View.GONE
                    searchBinding.textNoResult.visibility = View.VISIBLE
                } else {
                    searchBinding.searchResultRecyclerView.visibility = View.VISIBLE
                    searchBinding.textNoResult.visibility = View.GONE

                    baseFileAdapter.setBaseFileList(it)
                }
            }
        }
        searchViewModel.liveErrorData.observe(this) {
            // Hide ProgressBar first
            searchBinding.progressBar.visibility = View.GONE

            // Handle search error
            if (it != null) {
                Log.e(logTag, "Error Message Observed")
                Log.e(logTag, it.stackTraceToString())
                Toast.makeText(this, "Search Error: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

}