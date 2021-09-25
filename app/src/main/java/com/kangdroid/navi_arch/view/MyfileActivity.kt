package com.kangdroid.navi_arch.view

import android.os.Bundle
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.databinding.ActivityMyfileBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyfileActivity : PagerActivity() {

    protected val activityMyfileBinding: ActivityMyfileBinding by lazy {
        ActivityMyfileBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var myfilefragment : MyfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMyfileBinding.root)


        activityMyfileBinding.apply {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.myfile_framelayout, myfilefragment)
            transaction.commit()
        }
    }
}