package com.kangdroid.navi_arch.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    // View binding
    private val searchBinding: ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(searchBinding.root)
    }
}