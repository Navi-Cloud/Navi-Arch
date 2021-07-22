package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.viewmodel.PageRequest
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.util.FragmentTestUtil.startFragment
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible


@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class StartActivityTest {

    private lateinit var scenario : ActivityScenario<StartActivity>
    private val activity : StartActivity = Robolectric.setupActivity(StartActivity::class.java)

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private inline fun <reified T> getUserViewModel(receiver: T): UserViewModel {
        val memberProperty = T::class.declaredMembers.find { it.name == "userViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as UserViewModel
    }

    @Test
    fun is_ViewBinding_well() {
        scenario = ActivityScenario.launch(StartActivity::class.java).moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {
            assertNotNull(it.startBinding)
        }
        scenario.close()
    }

    @Test
    fun is_initObserver_MAIN_well(){

        val one = Robolectric.buildActivity(StartActivity::class.java)
            .create()
            .resume()
            .get()

        scenario = ActivityScenario.launch(StartActivity::class.java).moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {

            val userViewModel = getUserViewModel(it)
            userViewModel.requestMainPage()

            runCatching {
                userViewModel.pageRequest.getOrAwaitValue()
            }.onSuccess {
                assertThat(it).isEqualTo(PageRequest.REQUEST_MAIN)
            }.onFailure { throwable ->
                println(throwable.stackTraceToString())
            }

            val actual = Intent(one,MainActivity::class.java)
            val shadowactivity = shadowOf(one)
            val intent = shadowOf(shadowactivity.nextStartedActivity)
            assertEquals(intent.intentClass.name,MainActivity::class.java.name)
        }
    }


    @Test
    fun is_initObserver_LOGIN_well(){
        scenario = ActivityScenario.launch(StartActivity::class.java).moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {

            val userViewModel = getUserViewModel(it)

            runCatching {
                userViewModel.pageRequest.getOrAwaitValue()
            }.onSuccess {
                assertThat(it).isEqualTo(PageRequest.REQUEST_LOGIN)
            }.onFailure {
                println(it.stackTraceToString())
            }

//            val fragmentManager = activity.supportFragmentManager
//            val trans : FragmentTransaction = fragmentManager.beginTransaction()
//            trans.replace(R.id.startActivityContainer,loginFragment)

            assertNotNull(activity.transaction)

        }
    }

    @Test
    fun is_initObserver_REGISTER_well(){
        scenario = ActivityScenario.launch(StartActivity::class.java).moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {

            val userViewModel = getUserViewModel(it)
            userViewModel.requestRegisterPage()

            runCatching {
                userViewModel.pageRequest.getOrAwaitValue()
            }.onSuccess {
                assertThat(it).isEqualTo(PageRequest.REQUEST_REGISTER)
            }.onFailure { throwable ->
                println(throwable.stackTraceToString())
            }

            assertNotNull(activity.transaction)

//            val fragmentScenario = launchFragmentInContainer<RegisterFragment>(
//                themeResId = R.style.Theme_NaviArch // If you don't do this, this will throw error while inflating material view
//            )
//            assertNotNull(fragmentScenario)
        }

    }

}