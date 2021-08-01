package com.kangdroid.navi_arch.view

import android.R
import android.app.Activity.RESULT_OK
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.Menu
import android.widget.Button
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.PagerViewModel
import com.kangdroid.navi_arch.viewmodel.UploadingViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenu
import org.robolectric.fakes.RoboMenuItem
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible


/*
0. uploadingActivity로 intent 적용
1. 내 폴더로 intent 성공 -> ActivityResult.resultcode == RESULT_OK
2. 내 폴더로 intent 실패 -> RESULT_CANCELED
3. 업로딩 성공 -> fileUploadSucceed == true
*/

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

    private val mockFolderData: FileData = FileData(
        userId = "id",
        token = "token",
        prevToken = "prev",
        fileName = "name",
        fileType = "Folder"
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
    fun menu_inflate_is_well() {
        scenario = ActivityScenario.launch(UploadingActivity::class.java)
            .moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            val menu: Menu = RoboMenu(ApplicationProvider.getApplicationContext<Context>())
            val result = it.onCreateOptionsMenu(menu)

            assertEquals(result, true)
        }
    }

    //수정중..
    @Test
    open fun intent_is_well() {
        // Mock up an ActivityResult:
        val returnIntent = Intent()
        returnIntent.putExtra("finalUri", Uri.parse("test.com"))
        val activityResult: Instrumentation.ActivityResult = Instrumentation.ActivityResult(RESULT_OK, returnIntent)

        // Create an ActivityMonitor that catch ChildActivity and return mock ActivityResult:
        val activityMonitor: Instrumentation.ActivityMonitor = getInstrumentation().addMonitor(
            Intent.ACTION_GET_CONTENT,
            activityResult,
            true
        )

        // Simulate a button click that start ChildActivity for result:
        scenario = ActivityScenario.launch(UploadingActivity::class.java).moveToState(Lifecycle.State.STARTED)
        scenario.onActivity {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            // Wait for the ActivityMonitor to be hit, Instrumentation will then return the mock ActivityResult:
            val uploadingActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor,5)
            Assert.assertNotNull(uploadingActivity)
            // How do I check that StartActivityForResult correctly handles the returned result?
            assertThat(activityResult.resultData.data).isEqualTo(Uri.parse("test.com"))
        }
    }

    //여전히 수정중..
    @Test
    fun file_upload_doing_well() {
        scenario = ActivityScenario.launch(UploadingActivity::class.java)
            .moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            // Mock ServerManagement for UserViewModel
            val mockServerManagement: ServerManagement =
                mockk(relaxed = true) // relaxed mock returns some simple value for all functions
            val uploadingViewModel = getUploadingViewModel(it)
            ViewModelTestHelper.setFields(
                "serverManagement",
                uploadingViewModel,
                mockServerManagement
            )

            val pagerViewModel = getPagerViewModel(it)
            ViewModelTestHelper.setFields("serverManagement", pagerViewModel, mockServerManagement)

//            pagerViewModel.createInitialRootPage()
//            pagerViewModel.createInitialPage(mockFolderData)

            //set livePagerData
            val recyclerOnClickListener: ((FileData, Int) -> Unit) = {FileData, Int ->
            }
            val tmpList: List<FileData> = listOf(mockFolderData)
            val fileAdapter: FileAdapter = FileAdapter(
                onClick = recyclerOnClickListener,
                onLongClick = pagerViewModel.recyclerOnLongClickListener,
                fileList = tmpList,
                pageNumber = 1,
                currentFolder = mockFolderData
            )
            pagerViewModel.pageList.add(fileAdapter)
            pagerViewModel.livePagerData.value = pagerViewModel.pageList

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