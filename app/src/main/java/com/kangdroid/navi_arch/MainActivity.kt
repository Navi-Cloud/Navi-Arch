package com.kangdroid.navi_arch

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.pager.PagerAdapter
import com.kangdroid.navi_arch.pager.PagerViewModel
import com.kangdroid.navi_arch.recyclerview.FileAdapter

// The View
class MainActivity : AppCompatActivity() {

    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val pagerViewModel: PagerViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(PagerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // Pager Adapter
        val pageAdapter: PagerAdapter = PagerAdapter()
        activityMainBinding.viewPager.adapter = pageAdapter

        TabLayoutMediator(activityMainBinding.mainTab, activityMainBinding.viewPager) { tab, position ->
            tab.text = "test"
        }.attach()

        pagerViewModel.livePagerData.observe(this, Observer<List<FileAdapter>> {
            Log.d(this::class.java.simpleName, "Observed, Setting changed page")
            pageAdapter.setNaviPageList(it)
            activityMainBinding.viewPager.currentItem = it.lastIndex
        })

        pagerViewModel.createInitialRootPage()
    }
}