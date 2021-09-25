package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.adapter.PagerAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.databinding.ActivityMyfileBinding
import com.kangdroid.navi_arch.viewmodel.PagerViewModel

abstract class PagerActivity: AppCompatActivity() {

    protected val activityMyFileBinding: ActivityMyfileBinding by lazy {
        ActivityMyfileBinding.inflate(layoutInflater)
    }

    // Pager ViewModel
    protected val pagerViewModel: PagerViewModel by viewModels()

    // Pager Adapter[DI]
    private val pageAdapter: PagerAdapter = PagerAdapter()

    // Recycler On Long Click Listener
    open val recyclerOnLongClickListener: ((FileData) -> Boolean)? = null

    // Error Observer Callback
    open val errorObserverCallback: ((Throwable) -> Unit)? = null

    private var isFromSearch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMyFileBinding.root)


        initViewModel()

        if (intent.getSerializableExtra("searchFolder") != null) {
            isFromSearch = true
            val searchFolder: FileData = intent.getSerializableExtra("searchFolder") as FileData
            pagerViewModel.createInitialPage(searchFolder)
        } else {
            // Now start from initial page
            pagerViewModel.createInitialRootPage()
        }
    }


    private fun initViewModel() {
        // Set onLongClickListener to viewModel
//        pagerViewModel.recyclerOnLongClickListener = {
//            recyclerOnLongClickListener?.invoke(it) ?: false
//        }

        // Set Data Observer for pageData
        pagerViewModel.livePagerData.observe(this) {
                Log.d(this::class.java.simpleName, "Observed, Setting changed page")
                pageAdapter.setNaviPageList(it)
            }

            // Set Error Message Observer
            pagerViewModel.liveErrorData.observe(this) {
                Log.e(this::class.java.simpleName, "Error Message Observed")
                Log.e(this::class.java.simpleName, it.stackTraceToString())
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                errorObserverCallback?.invoke(it)
        }
    }
}