package com.kangdroid.navi_arch.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.adapter.PagerAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.databinding.ActivityMainBinding
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.PagerViewModel
import com.kangdroid.navi_arch.viewmodel.UploadingViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.HttpUrl
import javax.inject.Inject

// The View
@AndroidEntryPoint
class MainActivity : PagerActivity() {
    // Menu for dynamically hide - show
    private var dynamicMenu: Menu? = null

    override val recyclerOnLongClickListener: (FileData) -> Boolean = {
        val bottomSheetFragment: FileBottomSheetFragment = FileBottomSheetFragment(
            targetFileData = it,
            refreshPageLambda = {
                pagerViewModel.explorePage(
                    pagerViewModel.pageList[activityMainBinding.viewPager.currentItem].currentFolder,
                    activityMainBinding.viewPager.currentItem,
                    true
                )
            }
        )
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        true
    }

    override val errorObserverCallback: ((Throwable) -> Unit) = {
        dynamicMenu?.findItem(R.id.action_add_folder)?.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Enable Uploading Button when launched with MainActivity
        menuInflater.inflate(R.menu.main_action, menu)
        dynamicMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_folder -> {
                val menuBottomSheetFragment: MenuBottomSheetFragment = MenuBottomSheetFragment(
                    currentFolderToken = pagerViewModel.pageList[activityMainBinding.viewPager.currentItem].currentFolder.token,
                    refreshPage = {
                        pagerViewModel.explorePage(
                            pagerViewModel.pageList[activityMainBinding.viewPager.currentItem].currentFolder,
                            activityMainBinding.viewPager.currentItem,
                            true
                        )
                    }
                )

                menuBottomSheetFragment.show(supportFragmentManager, menuBottomSheetFragment.tag)
                true
            }
            R.id.action_search -> {
                // Search
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }
}