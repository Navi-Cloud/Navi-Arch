package com.kangdroid.navi_arch

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.fileView.FileViewModel
import com.kangdroid.navi_arch.recyclerview.FileAdapter

// The View
class MainActivity : AppCompatActivity() {

    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val fileViewModel: FileViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(FileViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // Recycler View
        val fileAdapter: FileAdapter = FileAdapter {
            Log.d(this::class.java.simpleName, "ViewModel Testing")
            Log.d(this::class.java.simpleName, "Token: ${it.token}")
        }
        activityMainBinding.naviMainRecyclerView.adapter = fileAdapter
        activityMainBinding.naviMainRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // View Model Init[Observe]
        fileViewModel.liveFileData.observe(this, Observer<List<FileData>> {
            // Update UI when data changed
            fileAdapter.setFileList(it)
        })

        // Start with Root
        fileViewModel.exploreRootData()
    }
}