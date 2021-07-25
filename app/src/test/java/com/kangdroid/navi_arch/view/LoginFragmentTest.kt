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
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible


@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {
    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher() // for coroutine test

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
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch // If you don't do this, this will throw error while inflating material view
        )
        scenario.moveToState(State.STARTED) //created, started, resumed, destroyed
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

        scenario.onFragment {
            val userViewModel: UserViewModel = getUserViewModel(it)
            it.onDestroyView()
            assertThat(userViewModel.loginErrorData.value).isEqualTo(null)
            assertThat(it.loginBinding).isEqualTo(null)
        }
        scenario.moveToState(State.DESTROYED)
    }

    @Test
    fun registerBtn_is_well(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch
        ).apply{
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

    @Test
    fun loginBtn_is_work_well(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState  = State.STARTED
        )

        scenario.onFragment{
            // Mock ServerManagement for UserViewModel
            val mockServerManagement: ServerManagement = mockk(relaxed = true) // relaxed mock returns some simple value for all functions
            val userViewModel: UserViewModel = getUserViewModel(it)
            ViewModelTestHelper.setFields("serverManagement", userViewModel, mockServerManagement)

            // Set
            it.loginBinding?.apply {
                idLogin.setText("userId")
                pwLogin.setText("userPw")
            }

            //Perform
            it.loginBinding?.button!!.performClick().also{ clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }

            // Get Live Data
            runCatching {
                userViewModel.pageRequest.getOrAwaitValue()
            }.onSuccess { pageRequest ->
                assertThat(pageRequest).isEqualTo(PageRequest.REQUEST_MAIN)
            }.onFailure { throwable ->
                println(throwable.stackTraceToString())
                Assertions.fail("This should be succeed...")
            }
        }
    }

    @Test
    fun loginBtn_is_fail(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch,
            initialState  = State.STARTED
        )

        scenario.onFragment{
            // Mock ServerManagement for UserViewModel
            val mockServerManagement: ServerManagement = mockk(relaxed = true) // relaxed mock returns some simple value for all functions
            val userViewModel: UserViewModel = getUserViewModel(it)
            ViewModelTestHelper.setFields("serverManagement", userViewModel, mockServerManagement)

            // Set values
            it.loginBinding?.apply {
                idLogin.setText("userId")
                pwLogin.setText("userPw")
            }
            userViewModel.loginErrorData.value = RuntimeException("")

            //Perform
            it.loginBinding?.button!!.performClick().also{ clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }

            // Get Data
            assertThat(it.loginBinding!!.idLogin.text.toString()).isEqualTo("")
            assertThat(it.loginBinding!!.pwLogin.text.toString()).isEqualTo("")
        }
    }
}