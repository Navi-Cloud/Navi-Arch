package com.navi.file.view.fragment

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navi.file.R
import com.navi.file.databinding.FragmentLoginBinding
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.intercommunication.DisplayScreen
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.viewmodel.AccountViewModel
import com.navi.file.viewmodel.LoginViewModel
import com.navi.file.viewmodel.ViewModelHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

class LoginFragmentFactory(
    private val loginViewModelFactory: ViewModelProvider.Factory,
    private val accountViewModelFactory: ViewModelProvider.Factory
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            LoginFragment::class.java.name -> LoginFragment(loginViewModelFactory, accountViewModelFactory)
            else -> super.instantiate(classLoader, className)
        }
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE)
class LoginFragmentTest: ViewModelTestHelper() {
    private lateinit var loginFragment: FragmentScenario<LoginFragment>
    private lateinit var mockLoginViewModel: LoginViewModel
    private lateinit var mockAccountViewModel: AccountViewModel

    private fun createFragmentScenario() {
        // Create Register Fragment Factory
        val loginFragmentFactory = LoginFragmentFactory(
            createViewModelFactory(mockLoginViewModel),
            createViewModelFactory(mockAccountViewModel)
        )

        // Setup Test
        loginFragment = launchFragmentInContainer(themeResId = R.style.Theme_NaviFile, factory = loginFragmentFactory)
    }

    @Before
    fun initTest() {
        mockLoginViewModel = mock()
        mockAccountViewModel = mock()
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

        // Do
        createFragmentScenario()

        // Do - Phase 2
        loginFragment.onFragment {
            val binding = getBinding<FragmentLoginBinding, LoginFragment>(it, "loginFragmentBinding").apply {
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

        // Do
        createFragmentScenario()

        // Do - Phase 2
        loginFragment.onFragment {
            val binding = getBinding<FragmentLoginBinding, LoginFragment>(it, "loginFragmentBinding").apply {
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

        // Do
        createFragmentScenario()

        // Do - Phase 2
        loginFragment.onFragment {
            val binding = getBinding<FragmentLoginBinding, LoginFragment>(it, "loginFragmentBinding")

            binding.textView2.performClick().also { result ->
                assertTrue(result)
            }

            mockDisplayData.getOrAwaitValue().also { result ->
                assertEquals(DisplayScreen.Register, result)
            }
        }
    }
}