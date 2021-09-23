package com.kangdroid.navi_arch.view

import android.os.Bundle
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.databinding.ActivityMyfileBinding

class MyfileActivity : PagerActivity() {

    protected val activityMyfileBinding: ActivityMyfileBinding by lazy {
        ActivityMyfileBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMyfileBinding.root)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.myfile_framelayout, MyfileFragment())
        transaction.commit()
    }
}