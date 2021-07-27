package com.kangdroid.navi_arch.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(searchBinding.root)

        initBinding()

        initObserver()
    }

    private fun initBinding() {
        searchBinding.apply {
            inputSearch.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_ENTER){
                    // Show ProgressBar
                    progressBar.visibility = View.VISIBLE

                    // Update sort mode
                    updateSortMode()

                    // Search file
                    val query: String = inputSearch.text.toString()
                    searchViewModel.search(
                        query = query,
                        mode = currentSortMode,
                        isReversed = isReversed
                    )
                    true
                } else false
            }
        }
    }

    private fun initObserver() {
        searchViewModel.searchResultLiveData.observe(this) {
            // Hide ProgressBar first
            searchBinding.progressBar.visibility = View.GONE

            // Handle search result
            if (it != null) {

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

    private fun updateSortMode() {
        currentSortMode = when (searchBinding.sortByNameOrLMT.isChecked) {
            true -> if (searchBinding.sortByType.isChecked) FileSortingMode.LMT else FileSortingMode.TypedLMT
            false -> if (searchBinding.sortByType.isChecked) FileSortingMode.Name else FileSortingMode.TypedName
        }
        isReversed = searchBinding.sortByAscendingOrDescending.isChecked
    }
}