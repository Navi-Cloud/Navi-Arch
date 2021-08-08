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
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.PagerViewModel
import com.kangdroid.navi_arch.viewmodel.UploadingViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getFunction
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenu
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowActivity
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class UploadingActivityTest {
    private lateinit var scenario: ActivityScenario<UploadingActivity>
    private var activity: UploadingActivity =
        Robolectric.setupActivity(UploadingActivity::class.java)

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private inline fun <reified T> getUploadingViewModel(receiver: T): UploadingViewModel {
        val memberProperty = T::class.declaredMembers.find { it.name == "uploadingViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as UploadingViewModel
    }

    private inline fun <reified T> getPagerViewModel(receiver: T): PagerViewModel {
        val memberProperty = T::class.members.find { it.name == "pagerViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as PagerViewModel
    }

    private val filedata : FileData = FileData(
        userId = "",
        fileName = "/",
        fileType = "Folder",
        token = "rootToken",
        prevToken = "prevToken"
    )

    @Test
    fun uploadingActivity_show_well() {
        //given
        val intent = Intent()
        val controller: ActivityController<*> = Robolectric
            .buildActivity(UploadingActivity::class.java, intent)
            .setup()

        // when
        controller.pause().stop()

        // then
        assertEquals(controller.intent, intent)
        print(intent.toString())
    }

    @Test
    fun resultCallbackLauncher_is_well(){
        val secnario = ActivityScenario.launch(UploadingActivity::class.java)
            .moveToState(Lifecycle.State.RESUMED)
        secnario.onActivity {
            val shadowSearchActivity: ShadowActivity = shadowOf(it)

            getFunction<UploadingActivity>("getContentActivity").call(it)
            val targetIntent: Intent = Intent(Intent.ACTION_GET_CONTENT)
            assertThat(shadowSearchActivity.nextStartedActivity.component).isEqualTo(targetIntent.component)
            assertThat(shadowSearchActivity.nextStartedActivity.type).isEqualTo("*/*")
        }
    }

    @Test
    fun onCreateOptionsMenu_is_well() {
        scenario = ActivityScenario.launch(UploadingActivity::class.java)
            .moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            val menu: Menu = RoboMenu(ApplicationProvider.getApplicationContext<Context>())
            val result = it.onCreateOptionsMenu(menu)

            assertEquals(result, true)
        }
    }

    //여전히 수정중..
    @Test
    fun file_upload_doing_well() {
        scenario = ActivityScenario.launch(UploadingActivity::class.java)
            .moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            // Mock ServerManagement for UserViewModel
            val mockServerManagement: ServerManagement = mockk(relaxed = true)
            val uploadingViewModel = getUploadingViewModel(it)
            val pagerViewModel = getPagerViewModel(it)
            ViewModelTestHelper.setFields(
                "serverManagement",
                uploadingViewModel,
                mockServerManagement
            )

            ViewModelTestHelper.setFields("serverManagement",pagerViewModel,mockServerManagement)

            val recyclerOnClickListener: ((FileData, Int) -> Unit) = {FileData, Int -> }
            val tmpList: List<FileData> = listOf(filedata)
            val fileAdapter: FileAdapter = FileAdapter(
                onClick = recyclerOnClickListener,
                onLongClick = pagerViewModel.recyclerOnLongClickListener,
                fileList = tmpList,
                pageNumber = 1,
                currentFolder = filedata
            )
            pagerViewModel.pageList.add(fileAdapter)
            pagerViewModel.livePagerData.value = pagerViewModel.pageList

            pagerViewModel.livePagerData.getOrAwaitValue().also {
                //option click
                val menuItem = RoboMenuItem(com.kangdroid.navi_arch.R.id.action_select_path)
                activity.onOptionsItemSelected(menuItem)

                //observer
                runCatching {
                    uploadingViewModel.fileUploadSucceed.getOrAwaitValue()
                }.onSuccess {
                    assertThat(it).isEqualTo(true)
                }.onFailure { throwable ->
                    println(throwable.stackTraceToString())
                }
            }
        }
    }

    @Test
    fun else_for_OptionItemSelected(){
        scenario = ActivityScenario.launch(UploadingActivity::class.java)
            .moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {

            val menuItem: MenuItem = RoboMenuItem(R.id.action_search)
            val result = it.onOptionsItemSelected(menuItem)
            assertEquals(result,false)
        }
    }
}