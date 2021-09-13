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

    private var isFromSearch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        activityMainBinding.apply {
            mainOption.setOnClickListener {
                val optionBottomSheetFragment = OptionBottomSheetFragment()

                optionBottomSheetFragment.show(supportFragmentManager, optionBottomSheetFragment.tag)
            }
        }
    }
}