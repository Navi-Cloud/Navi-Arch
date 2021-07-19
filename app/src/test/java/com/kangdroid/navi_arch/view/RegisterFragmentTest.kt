package com.kangdroid.navi_arch.view

import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.viewmodel.PageRequest
import com.kangdroid.navi_arch.viewmodel.PagerViewModel
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
//@RunWith(MockitoJUnitRunner::class)
@RunWith(AndroidJUnit4::class)
class RegisterFragmentTest {
    private val mockUserViewModel: UserViewModel = mock(UserViewModel::class.java)

    @Before
    fun setUp() {
        `when`(mockUserViewModel.register(anyString(), anyString(), anyString(), anyString()))
            .thenAnswer {
                mockUserViewModel.pageRequest.value = PageRequest.REQUEST_LOGIN
                Unit
            }
        `when`(mockUserViewModel.requestLoginPage())
            .thenAnswer {
                mockUserViewModel.pageRequest.value = PageRequest.REQUEST_LOGIN
                Unit
            }
    }

    @Test
    fun is_viewBinding_ok() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch // If you don't do this, this will throw error while inflating material view
        )
        scenario.moveToState(State.STARTED)
        scenario.onFragment{
            assertThat(it.registerBinding).isNotEqualTo(null)
        }
    }

    @Test
    fun is_onDestroyView_works_well(){
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch
        )
        scenario.onFragment {
            it.onDestroyView()
            assertThat(it.registerBinding).isEqualTo(null)
        }
        scenario.moveToState(State.DESTROYED)
    }

    @Test
    fun is_id_check_button_works_when_click() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            it.registerBinding?.button?.performClick().also { clickResult ->
                // TODO 아이디 중복 체크
                assertThat(clickResult).isEqualTo(true)
            }
        }
        scenario.moveToState(State.DESTROYED)
    }

    @Test
    fun is_register_button_works_when_all_input_args_ok() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set userViewModel of RegisterFragment [Trying]
            ViewModelTestHelper.setFields("userViewModel", it, mockUserViewModel)

            // Set values
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Name.setText("je")
                Email.setText("email@.com")
                Textpassword.setText("pw")
                passwordRe.setText("pw")
            }

            // Perform
            it.registerBinding?.button2!!.callOnClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }

            // Get Live Data
            runCatching {
                mockUserViewModel.pageRequest.getOrAwaitValue()
            }.onSuccess { pageRequest ->
                println(pageRequest)
                assertThat(pageRequest).isEqualTo(PageRequest.REQUEST_LOGIN)
            }.onFailure { throwable ->
                println(throwable.stackTraceToString())
                Assertions.fail("This should be succeed...")
            }
        }
    }

    @Test
    fun is_register_button_works_when_all_args_empty() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            it.registerBinding?.button2?.callOnClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }
            // TODO Assert
        }
    }

    @Test
    fun is_checkRegisterArgs_works_when_all_input_args_ok() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set values
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Name.setText("je")
                Email.setText("email@.com")
                Textpassword.setText("pw")
                passwordRe.setText("pw")
            }

            // Perform
            val result: Boolean =
                ViewModelTestHelper.getFunction<RegisterFragment>("checkRegisterArgs")
                    .call(it) as Boolean

            // Assert
            assertThat(result).isEqualTo(true)
        }
    }

    @Test
    fun is_checkRegisterArgs_works_when_invalid_email_form() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set values
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Name.setText("je")
                Email.setText("email") // "Email" must contains '@'
                Textpassword.setText("pw")
                passwordRe.setText("pw")
            }

            // Perform
            val result: Boolean =
                ViewModelTestHelper.getFunction<RegisterFragment>("checkRegisterArgs")
                    .call(it) as Boolean

            // Assert
            assertThat(result).isEqualTo(false)
        }
    }

    @Test
    fun is_checkRegisterArgs_works_when_password_not_equal() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set values
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Name.setText("je")
                Email.setText("email@.com")
                Textpassword.setText("pw1") // pw1
                passwordRe.setText("pw2")   // pw2 -> not equal
            }

            // Perform
            val result: Boolean =
                ViewModelTestHelper.getFunction<RegisterFragment>("checkRegisterArgs")
                    .call(it) as Boolean

            // Assert
            assertThat(result).isEqualTo(false)
        }
    }

    @Test
    fun is_checkRegisterArgs_works_when_all_args_empty() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Perform
            val result: Boolean =
                ViewModelTestHelper.getFunction<RegisterFragment>("checkRegisterArgs")
                    .call(it) as Boolean

            // Assert
            assertThat(result).isEqualTo(false)
        }
    }
}