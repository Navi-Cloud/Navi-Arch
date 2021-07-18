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
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.FragmentLoginBinding
import com.kangdroid.navi_arch.viewmodel.PageRequest
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import junit.framework.Assert.assertEquals
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.robolectric.annotation.Config


@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {
    private lateinit var userViewModel: UserViewModel

    @Before
    fun init() {
        userViewModel = UserViewModel()
    }

    @Test
    fun is_viewBinding_ok() {
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch // If you don't do this, this will throw error while inflating material view
        )
        scenario.moveToState(State.CREATED)
        scenario.onFragment{
            assertThat(it.loginBinding is FragmentLoginBinding?).isEqualTo(true)
            assertThat(it).isNotEqualTo(null)
        }
    }

    @Test
    fun is_onDestroyView_works_well(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch
        )
        scenario.onFragment {
            it.onDestroyView()
            assertThat(it.loginBinding is FragmentLoginBinding?).isEqualTo(true)
            assertThat(userViewModel.loginErrorData.value).isEqualTo(null)
            assertThat(it.loginBinding).isEqualTo(null)
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
            val button = it.view?.findViewById<TextView>(R.id.textView2)
            button?.performClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
                userViewModel.requestRegisterPage()
                assertThat(userViewModel.pageRequest.value).isEqualTo(PageRequest.REQUEST_REGISTER)
            }
        }
    }

    //로그인 버튼 눌렀을 때 에러
    @Test
    fun login_is_fail(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch
        ).apply {
            moveToState(State.STARTED)
        }

        scenario.onFragment {
            it.loginBinding!!.idLogin.setText("userid")
            it.loginBinding!!.pwLogin.setText("password")

            val exception = Throwable()
            userViewModel.loginError(exception)
            it.loginBinding!!.button.performClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }
            assertThat(userViewModel.loginErrorData.value).isNotEqualTo(null)
//            assertThat(it.loginBinding!!.idLogin.text.toString()).isEqualTo("")
//            assertThat(it.loginBinding!!.pwLogin.text.toString()).isEqualTo("")
        }
    }

    //로그인 버튼 눌렀을 때 성공
    @Test
    fun login_is_well(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch
        ).apply {
            moveToState(State.STARTED)
        }

        scenario.onFragment {
            it.loginBinding!!.idLogin.setText("userid")
            it.loginBinding!!.pwLogin.setText("password")

            it.loginBinding!!.button.performClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }

            assertThat(it.loginBinding!!.idLogin.text.toString()).isEqualTo("userid")
            assertThat(it.loginBinding!!.pwLogin.text.toString()).isEqualTo("password")
        }
    }

}