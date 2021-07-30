package com.kangdroid.navi_arch.view

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.PageRequest
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.util.concurrent.TimeoutException
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RegisterFragmentTest {

    // Since unit test can't use Main Looper, coroutine test should pass TestCoroutineDispatcher and execute within the runBlockingTest
    private val testDispatcher = TestCoroutineDispatcher() // for coroutine test

    // Rule that every android-thread should launched in single thread
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private inline fun<reified T> getUserViewModel(receiver: T): UserViewModel {
        val memberProperty = T::class.declaredMembers.find { it.name == "userViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as UserViewModel
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // swap dispatcher with a test dispatcher
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        testDispatcher.cleanupTestCoroutines()
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
    fun is_register_button_works_when_all_input_args_ok() = runBlockingTest {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Mock ServerManagement for UserViewModel
            val mockServerManagement: ServerManagement = mockk(relaxed = true) // relaxed mock returns some simple value for all functions

            // Get userViewModel and Set serverManagement (to mockServerManagement)
            val userViewModel: UserViewModel = getUserViewModel(it)
            ViewModelTestHelper.setFields("serverManagement", userViewModel, mockServerManagement)

            // Set values
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Name.setText("je")
                Email.setText("email@com")
                Textpassword.setText("pw")
                passwordRe.setText("pw")
            }

            // Perform
            it.registerBinding?.button2!!.callOnClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }

            // Get Live Data
            runCatching {
                userViewModel.pageRequest.getOrAwaitValue()
            }.onSuccess { pageRequest ->
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

            // Get userViewModel
            val userViewModel: UserViewModel = getUserViewModel(it)

            // Get Live Data
            // Since all args are empty, RegisterFragment don't call userViewModel.register()(: update pageRequest)
            runCatching {
                userViewModel.pageRequest.getOrAwaitValue()
            }.onSuccess {
                Assertions.fail("This should be failed...")
            }.onFailure { throwable ->
                assertThat(throwable is TimeoutException).isEqualTo(true)
            }
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
    fun is_checkRegisterArgs_works_when_check_policy_but_other_args_empty() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set value [only check policy box]
            it.registerBinding?.apply {
                checkbox.isChecked = true
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
    fun is_checkRegisterArgs_works_when_userId_empty() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set value [no userId]
            it.registerBinding?.apply {
                checkbox.isChecked = true
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
            assertThat(result).isEqualTo(false)
        }
    }

    @Test
    fun is_checkRegisterArgs_works_when_userName_empty() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set value [no userName]
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Email.setText("email@.com")
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
    fun is_checkRegisterArgs_works_when_userEmail_empty() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set value [no userEmail]
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Name.setText("je")
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
    fun is_checkRegisterArgs_works_when_userPassword_empty() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set value [no userPassword]
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Name.setText("je")
                Email.setText("email@.com")
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
    fun is_checkRegisterArgs_works_when_userPasswordForCheck_empty() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Set value [no userPasswordForCheck]
            it.registerBinding?.apply {
                checkbox.isChecked = true
                TextId.setText("id")
                Name.setText("je")
                Email.setText("email@.com")
                Textpassword.setText("pw")
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

    @Test
    fun is_back_press_works_well() {
        // Since back press works on activity, this test needs activity (start from activity)
        val activityController: ActivityController<StartActivity>
            = Robolectric.buildActivity(StartActivity::class.java)

        val startActivity: StartActivity = activityController
            .create()
            .start()
            .resume()
            .get()

        // Get userViewModel
        val userViewModel: UserViewModel = getUserViewModel(startActivity)

        // Fragment transaction to RegisterFragment !
        userViewModel.requestRegisterPage()
        assertThat(userViewModel.pageRequest.getOrAwaitValue()).isEqualTo(PageRequest.REQUEST_REGISTER)
        //startActivity.supportFragmentManager.beginTransaction().apply {
        //    add(startActivity.registerFragment, "RegisterFragment")
        //    commit()
        //}

        // BackPress at StartActivity[with RegisterFragment] will callback to onBackPressedDispatcher of RegisterFragment: requestLoginPage()
        startActivity.onBackPressed()
        assertThat(userViewModel.pageRequest.getOrAwaitValue()).isEqualTo(PageRequest.REQUEST_LOGIN)
    }

    @Test
    fun is_registerErrorData_observe_well_when_not_null() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Get userViewModel
            val userViewModel: UserViewModel = getUserViewModel(it)

            // Perform [change registerErrorData]
            val testRegisterErrorMsg: String = "test"
            userViewModel.registerErrorData.value = RuntimeException(testRegisterErrorMsg)

            // Assert
            assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Register Error: $testRegisterErrorMsg")
        }
    }

    @Test
    fun is_registerErrorData_observe_well_null() {
        val scenario = launchFragmentInContainer<RegisterFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState = State.STARTED
        )
        scenario.onFragment{
            // Get userViewModel
            val userViewModel: UserViewModel = getUserViewModel(it)

            // Perform [change registerErrorData to null]
            userViewModel.registerErrorData.value = null

            // Assert
            assertThat(ShadowToast.shownToastCount()).isEqualTo(0)
        }
    }
}