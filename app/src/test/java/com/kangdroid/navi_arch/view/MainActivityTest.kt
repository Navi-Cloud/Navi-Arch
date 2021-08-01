package com.kangdroid.navi_arch.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.utils.PagerCacheUtils
import com.kangdroid.navi_arch.viewmodel.PagerViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import junit.framework.Assert.*
import okhttp3.HttpUrl
import org.assertj.core.api.Assertions
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.TextLayoutMode
import org.robolectric.fakes.RoboMenu
import org.robolectric.fakes.RoboMenuItem
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var scenario : ActivityScenario<MainActivity>
    private val mainactivity : MainActivity = Robolectric.setupActivity(MainActivity::class.java)
    private val pageractivity : PagerActivity = Robolectric.setupActivity(MainActivity::class.java)

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    companion object {
        @JvmStatic
        val serverSetup: ServerSetup = ServerSetup(
            if (System.getProperty("os.name").contains("Windows")) {
                WindowsServerSetup()
            } else {
                LinuxServerSetup()
            }
        )

        @JvmStatic
        @BeforeClass
        fun setupServer() {
            println("Setting up server..")
            serverSetup.setupServer()
            println("Setting up server finished!")
        }

        @JvmStatic
        @AfterClass
        fun clearServer() {
            println("Clearing Server..")
            serverSetup.killServer(false)
            println("Clearing Server finished!")
        }
    }

    private inline fun <reified T> getPagerViewModel(receiver: T): PagerViewModel {
        val memberProperty = T::class.declaredMembers.find { it.name == "pagerViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as PagerViewModel
    }

    private val serverManagement: ServerManagement = ServerManagement.getServerManagement(
        HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(8080)
            .build()
    )

    // Mock Register Request
    private val mockUserRegisterRequest: RegisterRequest = RegisterRequest(
        userId = "kangdroid",
        userPassword = "test",
        userEmail = "Ttest",
        userName = "KangDroid"
    )

    private fun registerAndLogin() {
        serverManagement.register(mockUserRegisterRequest)
        serverManagement.loginUser(
            LoginRequest(
                userId = mockUserRegisterRequest.userId,
                userPassword = mockUserRegisterRequest.userPassword
            )
        )
    }

    @Before
    fun init() {
       serverSetup.clearData()
        ViewModelTestHelper.setFields("serverManagement", getPagerViewModel(pageractivity), serverManagement)
    }

    @After
    fun destroy() {
        serverSetup.clearData()
    }

    @Test
    fun is_recyclerOnLongClickListener_well(){
        scenario = ActivityScenario.launch(MainActivity::class.java).moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {

            registerAndLogin()
            val filedata : FileData = FileData(
                userId = "",
                fileName = "/",
                fileType = "Folder",
                token = "rootToken",
                prevToken = "prevToken"
            )

            // working....
            getPagerViewModel(it).createInitialRootPage()

            val result = it.recyclerOnLongClickListener(filedata)
            assertEquals(result,true)
        }
    }

    @Test
    fun is_errorObserverCallback_well() {

        scenario = ActivityScenario.launch(MainActivity::class.java).moveToState(Lifecycle.State.STARTED)
        scenario.onActivity {
            it.errorObserverCallback(Throwable())
            val dynamicmenu: Menu = ViewModelTestHelper.getFields("dynamicMenu", it)
            assertEquals(dynamicmenu.findItem(R.id.action_add_folder).isVisible,false)
        }

    }

    @Test
    fun is_onCreateOptionsMenu_well(){

        scenario = ActivityScenario.launch(MainActivity::class.java).moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {

            val menu : Menu = RoboMenu(ApplicationProvider.getApplicationContext<Context>())
            it.onCreateOptionsMenu(menu)
            val dynamicmenu: Menu = ViewModelTestHelper.getFields("dynamicMenu", it)
            assertEquals(dynamicmenu,menu)
        }

    }

//    @Test
//    fun addfolder_for_OptionItemSelected(){
//        scenario = ActivityScenario.launch(MainActivity::class.java).moveToState(Lifecycle.State.RESUMED)
//        scenario.onActivity {
//
//            val menuItem: MenuItem = RoboMenuItem(R.id.action_add_folder)
//            val result = it.onOptionsItemSelected(menuItem)
//            print(pageractivity.pagerViewModel.pageList[
//                    pageractivity.activityMainBinding.viewPager.currentItem].currentFolder.token)
//
//            assertEquals(result,true)
//        }
//    }

    @Test
    fun search_for_OptionItemSelected(){
        val actual = Intent(mainactivity,SearchActivity::class.java)

        scenario = ActivityScenario.launch(MainActivity::class.java).moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {

            val menuItem: MenuItem = RoboMenuItem(R.id.action_search)
            val result = it.onOptionsItemSelected(menuItem)

            val shadowactivity = shadowOf(it)
            val intent = shadowactivity.nextStartedActivity
            assertEquals(intent.component,actual.component)
            assertEquals(result,true)
        }
    }

    @Test
    fun else_for_OptionItemSelected(){
        scenario = ActivityScenario.launch(MainActivity::class.java).moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {

            val menuItem: MenuItem = RoboMenuItem(R.id.action_select_path)
            val result = it.onOptionsItemSelected(menuItem)
            assertEquals(result,false)
        }
    }
}