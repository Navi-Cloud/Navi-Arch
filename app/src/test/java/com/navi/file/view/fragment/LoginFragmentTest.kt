package com.navi.file.view.fragment

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.core.util.Preconditions
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.navi.file.HiltTestActivity
import com.navi.file.R
import com.navi.file.databinding.FragmentLoginBinding
import com.navi.file.helper.ViewModelFactory
import com.navi.file.hilt.ViewModelFactoryModule
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.intercommunication.DisplayScreen
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.viewmodel.AccountViewModel
import com.navi.file.viewmodel.LoginViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import javax.inject.Singleton

@UninstallModules(ViewModelFactoryModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE, application = HiltTestApplication::class)
class LoginFragmentTest: ViewModelTestHelper() {
    private lateinit var mockLoginViewModel: LoginViewModel
    private lateinit var mockAccountViewModel: AccountViewModel

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Module
    @InstallIn(SingletonComponent::class)
    inner class ViewModelFactoryTestModule {
        @Provides
        @Singleton
        fun provideTestFactory(): ViewModelFactory {
            return mock {
                on {loginViewModelFactory} doReturn(createViewModelFactory(mockLoginViewModel))
                on {accountViewModelFactory} doReturn(createViewModelFactory(mockAccountViewModel))
            }
        }
    }

    @Before
    fun initTest() {
        mockLoginViewModel = mock()
        mockAccountViewModel = mock()
        hiltRule.inject()
    }

    @Test
    fun `when user login succeeds, it should literally do NOTHING`() {
        // Setup Live Data
        val mockLiveData = MutableLiveData<ExecutionResult<UserLoginResponse>>()
        whenever(mockLoginViewModel.loginUser).thenReturn(mockLiveData)

        // Setup ViewModel Function
        whenever(mockLoginViewModel.requestUserLogin(any(), any())).thenAnswer {
            mockLiveData.value = ExecutionResult(ResultType.Success, null, "")
            null
        }

        launchFragmentInHiltContainer<LoginFragment> {
            val binding = getBinding<FragmentLoginBinding, LoginFragment>(this, "loginFragmentBinding").apply {
                loginEmailInput.editText?.setText("test")
                loginPasswordInput.editText?.setText("test")
            }

            binding.loginButton.performClick().also { result ->
                assertTrue(result)
            }

            assertEquals("test", binding.loginEmailInput.editText?.text.toString())
            assertEquals("test", binding.loginPasswordInput.editText?.text.toString())
        }
    }

    @Test
    fun `when user login fails, it should set every field to empty and show toast`() {
        // Setup Live Data
        val mockLiveData = MutableLiveData<ExecutionResult<UserLoginResponse>>()
        whenever(mockLoginViewModel.loginUser).thenReturn(mockLiveData)

        // Setup ViewModel Function
        whenever(mockLoginViewModel.requestUserLogin(any(), any())).thenAnswer {
            mockLiveData.value = ExecutionResult(ResultType.Forbidden, null, "")
            null
        }

        // Do - Phase 2
        launchFragmentInHiltContainer<LoginFragment> {
            val binding = getBinding<FragmentLoginBinding, LoginFragment>(this, "loginFragmentBinding").apply {
                loginEmailInput.editText?.setText("test")
                loginPasswordInput.editText?.setText("test")
            }

            binding.loginButton.performClick().also { result ->
                assertTrue(result)
            }

            assertEquals("", binding.loginEmailInput.editText?.text.toString())
            assertEquals("", binding.loginPasswordInput.editText?.text.toString())
            assertEquals("", ShadowToast.getTextOfLatestToast())
        }
    }

    @Test
    fun `when user try to click register, then it should set display screen live data to register`() {
        // Setup Live Data
        val mockLiveData = MutableLiveData<ExecutionResult<UserLoginResponse>>()
        val mockDisplayData = MutableLiveData<DisplayScreen>()
        whenever(mockLoginViewModel.loginUser).thenReturn(mockLiveData)
        whenever(mockAccountViewModel.displayLiveData).thenReturn(mockDisplayData)

        // Do - Phase 2
        launchFragmentInHiltContainer<LoginFragment> {
            val binding = getBinding<FragmentLoginBinding, LoginFragment>(this, "loginFragmentBinding")

            binding.textView2.performClick().also { result ->
                assertTrue(result)
            }

            mockDisplayData.getOrAwaitValue().also { result ->
                assertEquals(DisplayScreen.Register, result)
            }
        }
    }
}