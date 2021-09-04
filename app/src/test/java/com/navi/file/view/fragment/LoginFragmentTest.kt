package com.navi.file.view.fragment

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.FragmentScenario.Companion.launchInContainer
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navi.file.R
import com.navi.file.databinding.FragmentLoginBinding
import com.navi.file.databinding.FragmentRegisterBinding
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

class LoginFragmentFactory(
    private val userViewModelFactory: UserViewModelFactory
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            LoginFragment::class.java.name -> LoginFragment(userViewModelFactory)
            else -> super.instantiate(classLoader, className)
        }
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE)
class LoginFragmentTest {
    private lateinit var loginFragment: FragmentScenario<LoginFragment>
    private lateinit var mockUserViewModel: UserViewModel

    private fun getBinding(objectCall: LoginFragment): FragmentLoginBinding {
        val memberProperty = LoginFragment::class.declaredMembers.find {it.name == "loginFragmentBinding"}!!.apply {
            isAccessible = true
        }

        return memberProperty.call(objectCall) as FragmentLoginBinding
    }

    private fun createFragmentScenario() {
        // Create Register Fragment Factory
        val loginFragmentFactory = LoginFragmentFactory(
            userViewModelFactory = UserViewModelFactory(
                mockUserViewModel = mockUserViewModel
            )
        )

        // Setup Test
        loginFragment = launchFragmentInContainer(themeResId = R.style.Theme_NaviFile, factory = loginFragmentFactory)
    }

    @Before
    fun initTest() {
        mockUserViewModel = mock()
    }

    @Test
    fun `when user login succeeds, it should literally do NOTHING`() {
        // Setup Live Data
        val mockLiveData = MutableLiveData<ExecutionResult<UserLoginResponse>>()
        whenever(mockUserViewModel.loginUser).thenReturn(mockLiveData)

        // Setup ViewModel Function
        whenever(mockUserViewModel.requestUserLogin(any())).thenAnswer {
            mockLiveData.value = ExecutionResult(ResultType.Success, null, "")
            null
        }

        // Do
        createFragmentScenario()

        // Do - Phase 2
        loginFragment.onFragment {
            val binding = getBinding(it).apply {
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
        whenever(mockUserViewModel.loginUser).thenReturn(mockLiveData)

        // Setup ViewModel Function
        whenever(mockUserViewModel.requestUserLogin(any())).thenAnswer {
            mockLiveData.value = ExecutionResult(ResultType.Forbidden, null, "")
            null
        }

        // Do
        createFragmentScenario()

        // Do - Phase 2
        loginFragment.onFragment {
            val binding = getBinding(it).apply {
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
}