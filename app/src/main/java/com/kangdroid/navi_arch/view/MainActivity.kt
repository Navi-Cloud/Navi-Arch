package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

// The View
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    protected val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        activityMainBinding.apply {
            mainOption.setOnClickListener {
                val optionBottomSheetFragment = OptionBottomSheetFragment()
                optionBottomSheetFragment.show(supportFragmentManager, optionBottomSheetFragment.tag)
            }
            mainSearchBar.setOnClickListener{
                val intent = Intent(applicationContext,SearchActivity::class.java)
                startActivity(intent)
            }
            mainMyfile.setOnClickListener {
                val intent = Intent(applicationContext,MyfileActivity::class.java)
                startActivity(intent)
            }
        }
    }
}