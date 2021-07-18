package com.kangdroid.navi_arch.view

//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.matcher.ViewMatchers.withText
//import androidx.test.espresso.matcher.ViewMatchers.withId
//import androidx.test.espresso.action.ViewActions.click
//
//import androidx.test.espresso.assertion.ViewAssertions.matches

import android.os.Build
import android.os.IBinder
import android.provider.DocumentsContract
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.FragmentLoginBinding
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.viewmodel.PageRequest
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.setFields
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {
    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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

    private val serverManagement: ServerManagement = ServerManagement.getServerManagement(
        HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(8080)
            .build()
    )

    private inline fun<reified T> getUserViewModel(receiver: T): UserViewModel {
        val memberProperty = T::class.declaredMembers.find { it.name == "userViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as UserViewModel
    }

    @Test
    fun is_viewBinding_ok() {
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch // If you don't do this, this will throw error while inflating material view
        )
        scenario.moveToState(State.CREATED)
        scenario.onFragment{
            assertThat(it.loginBinding).isNotEqualTo(null)
            assertThat(it).isNotEqualTo(null)
        }
    }

    @Test
    fun is_onDestroyView_works_well(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch
        )

        scenario.onFragment {loginFragment ->
            val userViewModel: UserViewModel = getUserViewModel(loginFragment)
            loginFragment.onDestroyView()
            assertThat(userViewModel.loginErrorData.value).isEqualTo(null)
            assertThat(loginFragment.loginBinding).isEqualTo(null)
        }
        scenario.moveToState(State.DESTROYED)
    }

    @Test
    fun show_registerView(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch
        ).apply {
            moveToState(State.STARTED)
        }
        scenario.onFragment {
            val userViewModel: UserViewModel = getUserViewModel(it)
            assertThat(it.loginBinding).isNotEqualTo(null)

            it.loginBinding!!.textView2.performClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }
            assertThat(userViewModel.pageRequest.getOrAwaitValue()).isEqualTo(PageRequest.REQUEST_REGISTER)
        }
    }

    //로그인 버튼 눌렀을 때 에러
    @Test
    fun login_is_fail(){
        ShadowLog.stream = System.out

        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch
        ).apply {
            moveToState(State.STARTED)
        }

        scenario.onFragment {
            it.loginBinding!!.idLogin.setText("userid")
            it.loginBinding!!.pwLogin.setText("password")
            val userViewModel: UserViewModel = getUserViewModel(it)
            setFields("serverManagement", userViewModel, serverManagement)
            assertThat(it.loginBinding!!.button.performClick()).isEqualTo(true)
            userViewModel.loginErrorData.value = RuntimeException("")

            assertThat(it.loginBinding!!.idLogin.text.toString()).isEqualTo("")
            assertThat(it.loginBinding!!.pwLogin.text.toString()).isEqualTo("")
        }
    }
//
//    //로그인 버튼 눌렀을 때 성공
//    @Test
//    fun login_is_well(){
//        val scenario = launchFragmentInContainer<LoginFragment>(
//            themeResId = R.style.Theme_NaviArch
//        ).apply {
//            moveToState(State.STARTED)
//        }
//
//        scenario.onFragment {
//            it.loginBinding!!.idLogin.setText("userid")
//            it.loginBinding!!.pwLogin.setText("password")
//
//            it.loginBinding!!.button.performClick().also { clickResult ->
//                assertThat(clickResult).isEqualTo(true)
//            }
//
//            assertThat(it.loginBinding!!.idLogin.text.toString()).isEqualTo("userid")
//            assertThat(it.loginBinding!!.pwLogin.text.toString()).isEqualTo("password")
//        }
//    }

}