package com.kangdroid.navi_arch.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.ActivityStartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {
    // View binding
    private val startBinding: ActivityStartBinding by lazy {
        ActivityStartBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(startBinding.root)
    }
}