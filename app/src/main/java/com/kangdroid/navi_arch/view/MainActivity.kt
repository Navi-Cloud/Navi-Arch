package com.kangdroid.navi_arch.view

import android.view.*
import com.kangdroid.navi_arch.R
import dagger.hilt.android.AndroidEntryPoint

// The View
@AndroidEntryPoint
class MainActivity : PagerActivity() {
    // Menu for dynamically hide - show
    private var dynamicMenu: Menu? = null

    override val errorObserverCallback: ((Throwable) -> Unit) = {
        dynamicMenu?.findItem(R.id.action_add_folder)?.isVisible = false
    }
}