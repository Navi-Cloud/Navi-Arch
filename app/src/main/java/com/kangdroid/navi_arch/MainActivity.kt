package com.kangdroid.navi_arch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.fileView.FileViewModel

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

        fileViewModel.liveFileData.observe(this, Observer<List<FileData>> {
            // Update UI when data changed
        })
    }
}