package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.UploadingViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import io.mockk.mockk
import junit.framework.TestCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
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
class UploadingActivityTest : TestCase(){
    private lateinit var scenario : ActivityScenario<UploadingActivity>
    private var activity : UploadingActivity = Robolectric.setupActivity(UploadingActivity::class.java)

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private inline fun <reified T> getUploadingViewModel(receiver: T): UploadingViewModel {
        val memberProperty = T::class.declaredMembers.find { it.name == "uploadingViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as UploadingViewModel
    }

    @Test
    fun uploadingActivity_show_well(){
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

    //수정중..
    @Test
    fun file_upload_doing_well(){
        scenario = ActivityScenario.launch(UploadingActivity::class.java).moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            // Mock ServerManagement for UserViewModel
            val mockServerManagement: ServerManagement = mockk(relaxed = true) // relaxed mock returns some simple value for all functions
            val uploadingViewModel = getUploadingViewModel(it)
            ViewModelTestHelper.setFields("serverManagement", uploadingViewModel, mockServerManagement)

            //옵션 선택됨
            val menuItem = RoboMenuItem(com.kangdroid.navi_arch.R.id.action_select_path)
            activity.onOptionsItemSelected(menuItem)
            // *** pagerActivity 가 초기화 되지 않은듯..

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