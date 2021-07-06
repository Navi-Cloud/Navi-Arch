package com.kangdroid.navi_arch.view

import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.kangdroid.navi_arch.adapter.PagerAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.viewmodel.PagerViewModel

abstract class PagerActivity: AppCompatActivity() {
    // View Binding
    protected val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // Pager ViewModel
    protected val pagerViewModel: PagerViewModel by viewModels()

    // Pager Adapter[DI]
    private val pageAdapter: PagerAdapter = PagerAdapter()

    // Recycler On Long Click Listener
    open val recyclerOnLongClickListener: ((FileData) -> Boolean)? = null

    // Error Observer Callback
    open val errorObserverCallback: ((Throwable) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // Init Pager Adapter
        initPager()

        // Init View Model
        initViewModel()

        // Init toggle button
        initToggleButton()

        // Now start from initial page
        pagerViewModel.createInitialRootPage()
    }

    private fun initPager() {
        activityMainBinding.viewPager.adapter = pageAdapter

        TabLayoutMediator(
            activityMainBinding.mainTab,
            activityMainBinding.viewPager
        ) { tab, position ->
            tab.text = if (position == 0) {
                "/"
            } else {
                pagerViewModel.livePagerData.value?.get(position)?.currentFolder?.getBriefName()
            }
        }.attach()
    }

    private fun initViewModel() {
        // Set onLongClickListener to viewModel
        pagerViewModel.recyclerOnLongClickListener = {
            recyclerOnLongClickListener?.invoke(it) ?: false
        }

        // Set Data Observer for pageData
        pagerViewModel.livePagerData.observe(this) {
            Log.d(this::class.java.simpleName, "Observed, Setting changed page")
            pageAdapter.setNaviPageList(it)
            activityMainBinding.viewPager.currentItem = it.lastIndex
        }

        // Set Error Message Observer
        pagerViewModel.liveErrorData.observe(this) {
            Log.e(this::class.java.simpleName, "Error Message Observed")
            Log.e(this::class.java.simpleName, it.stackTraceToString())
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            errorObserverCallback?.invoke(it)
        }
    }

    private fun initToggleButton() {
        // Toggle Buttons[Sorting Buttons]
        val sortListener: (CompoundButton, Boolean) -> Unit = { _, _ ->
            pagerViewModel.sort(
                when (activityMainBinding.sortByNameOrLMT.isChecked) {
                    true -> if (activityMainBinding.sortByType.isChecked) FileSortingMode.LMT else FileSortingMode.TypedLMT
                    false -> if (activityMainBinding.sortByType.isChecked) FileSortingMode.Name else FileSortingMode.TypedName
                },
                activityMainBinding.sortByAscendingOrDescending.isChecked,
                activityMainBinding.viewPager.currentItem
            )
        }

        // Toggle Button INIT
        with(activityMainBinding) {
            sortByNameOrLMT.setOnCheckedChangeListener(sortListener)
            sortByType.setOnCheckedChangeListener(sortListener)
            sortByAscendingOrDescending.setOnCheckedChangeListener(sortListener)
        }
    }
}