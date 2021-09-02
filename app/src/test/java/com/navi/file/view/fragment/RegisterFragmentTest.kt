package com.navi.file.view.fragment

import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navi.file.R
import com.navi.file.databinding.FragmentRegisterBinding
import com.navi.file.model.UserRegisterRequest
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.viewmodel.UserViewModel
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

class UserViewModelFactory(
    private val mockUserViewModel: UserViewModel
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            mockUserViewModel as T
        } else {
            throw IllegalStateException()
        }
    }
}

class RegisterFragmentFactory(
    private val userViewModelFactory: UserViewModelFactory
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            RegisterFragment::class.java.name -> RegisterFragment(userViewModelFactory)
            else -> super.instantiate(classLoader, className)
        }
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE)
class RegisterFragmentTest {
    private lateinit var registerFragment: FragmentScenario<RegisterFragment>
    private lateinit var mockUserViewModel: UserViewModel

    private fun createFragmentScenario() {
        // Create Register Fragment Factory
        val registerFragmentFactory = RegisterFragmentFactory(
            userViewModelFactory = UserViewModelFactory(
                mockUserViewModel = mockUserViewModel
            )
        )

        // Setup Test
        registerFragment = launchFragmentInContainer(themeResId = R.style.Theme_NaviFile, factory = registerFragmentFactory)
    }

    private fun getBinding(objectCall: RegisterFragment): FragmentRegisterBinding {
        val memberProperty = RegisterFragment::class.declaredMembers.find {it.name == "registerBinding"}!!.apply {
            isAccessible = true
        }

        return memberProperty.call(objectCall) as FragmentRegisterBinding
    }

    @Before
    fun initTest() {
        // Create Initial Mock
        mockUserViewModel = mock(UserViewModel::class.java)
    }

    @Test
    fun `when model validation error occurred during registration, all edit text should be altered`() {
        // Let
        val mockRegisterResult = MutableLiveData<ExecutionResult<ResponseBody>>()
        val mockRequest = UserRegisterRequest("testEmail", "testName", "testPassword")

        // Setup Live Data
        `when`(mockUserViewModel.registerResult).thenReturn(mockRegisterResult)

        // Setup Request
        `when`(mockUserViewModel.requestUserRegister(mockRequest)).thenAnswer {
            mockRegisterResult.value = ExecutionResult(ResultType.ModelValidateFailed, value = null, "")
            null
        }

        // Create Fragment
        createFragmentScenario()

        // do
        registerFragment.onFragment {
            val binding = getBinding(it).apply {
                emailInputLayout.editText?.setText(mockRequest.userEmail)
                inputNameLayout.editText?.setText(mockRequest.userName)
                inputPasswordLayout.editText?.setText(mockRequest.userPassword)
            }

            // Click it
            binding.confirmButton.performClick().also { result ->
                Assert.assertTrue(result)
            }

            // Check it
            assertEquals("", binding.emailInputLayout.editText?.text.toString())
            assertEquals("", binding.inputNameLayout.editText?.text.toString())
            assertEquals("", binding.inputPasswordLayout.editText?.text.toString())
            assertEquals("", ShadowToast.getTextOfLatestToast().toString())
        }
    }

    @Test
    fun `when CONFLICT error occurred during registration, all edit text should be altered`() {
        // Let
        val mockRegisterResult = MutableLiveData<ExecutionResult<ResponseBody>>()
        val mockRequest = UserRegisterRequest("testEmail", "testName", "testPassword")

        // Setup Live Data
        `when`(mockUserViewModel.registerResult).thenReturn(mockRegisterResult)

        // Setup Request
        `when`(mockUserViewModel.requestUserRegister(mockRequest)).thenAnswer {
            mockRegisterResult.value = ExecutionResult(ResultType.Conflict, value = null, "")
            null
        }

        // Create Fragment
        createFragmentScenario()

        // do
        registerFragment.onFragment {
            val binding = getBinding(it).apply {
                emailInputLayout.editText?.setText(mockRequest.userEmail)
                inputNameLayout.editText?.setText(mockRequest.userName)
                inputPasswordLayout.editText?.setText(mockRequest.userPassword)
            }

            // Click it
            binding.confirmButton.performClick().also { result ->
                Assert.assertTrue(result)
            }

            // Check it
            assertEquals("", binding.emailInputLayout.editText?.text.toString())
            assertEquals("", binding.inputNameLayout.editText?.text.toString())
            assertEquals("", binding.inputPasswordLayout.editText?.text.toString())
            assertEquals("", ShadowToast.getTextOfLatestToast().toString())
        }
    }

    @Test
    fun `when register succeeds, it should do nothing, literally nothing for now`() {
        // Let
        val mockRegisterResult = MutableLiveData<ExecutionResult<ResponseBody>>()
        val mockRequest = UserRegisterRequest("testEmail", "testName", "testPassword")

        // Setup Live Data
        `when`(mockUserViewModel.registerResult).thenReturn(mockRegisterResult)

        // Setup Request
        `when`(mockUserViewModel.requestUserRegister(mockRequest)).thenAnswer {
            mockRegisterResult.value = ExecutionResult(ResultType.Success, value = null, "")
            null
        }

        // Create Fragment
        createFragmentScenario()

        // do
        registerFragment.onFragment {
            val binding = getBinding(it).apply {
                emailInputLayout.editText?.setText(mockRequest.userEmail)
                inputNameLayout.editText?.setText(mockRequest.userName)
                inputPasswordLayout.editText?.setText(mockRequest.userPassword)
            }

            // Click it
            binding.confirmButton.performClick().also { result ->
                Assert.assertTrue(result)
            }

            // Check it
            assertEquals(mockRequest.userEmail, binding.emailInputLayout.editText?.text.toString())
            assertEquals(mockRequest.userName, binding.inputNameLayout.editText?.text.toString())
            assertEquals(mockRequest.userPassword, binding.inputPasswordLayout.editText?.text.toString())
        }
    }
}