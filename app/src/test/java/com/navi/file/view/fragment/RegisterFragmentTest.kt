package com.navi.file.view.fragment

import android.os.Build
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navi.file.databinding.FragmentRegisterBinding
import com.navi.file.helper.ViewModelFactory
import com.navi.file.hilt.ViewModelFactoryModule
import com.navi.file.model.UserRegisterRequest
import com.navi.file.model.intercommunication.DisplayScreen
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.viewmodel.AccountViewModel
import com.navi.file.viewmodel.RegisterViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Assert.assertEquals
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
class RegisterFragmentTest: ViewModelTestHelper() {
    private lateinit var mockRegisterViewModel: RegisterViewModel
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
                on {registerViewModelFactory} doReturn(createViewModelFactory(mockRegisterViewModel))
                on {accountViewModelFactory} doReturn(createViewModelFactory(mockAccountViewModel))
            }
        }
    }

    @Before
    fun initTest() {
        // Create Initial Mock
        mockRegisterViewModel = mock()
        mockAccountViewModel = mock()
    }

    @Test
    fun `when model validation error occurred during registration, all edit text should be altered`() {
        // Let
        val mockRegisterResult = MutableLiveData<ExecutionResult<ResponseBody>>()
        val mockRequest = UserRegisterRequest("testEmail", "testName", "testPassword")

        // Setup Live Data
        whenever(mockRegisterViewModel.registerResult).thenReturn(mockRegisterResult)

        // Setup Request
        whenever(mockRegisterViewModel.requestUserRegister(any(), any(), any())).thenAnswer {
            mockRegisterResult.value = ExecutionResult(ResultType.ModelValidateFailed, value = null, "")
            null
        }

        // do
        launchFragmentInHiltContainer<RegisterFragment>{
            val binding = getBinding<FragmentRegisterBinding, RegisterFragment>(this, "registerBinding").apply {
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
        whenever(mockRegisterViewModel.registerResult).thenReturn(mockRegisterResult)

        // Setup Request
        whenever(mockRegisterViewModel.requestUserRegister(any(), any(), any())).thenAnswer {
            mockRegisterResult.value = ExecutionResult(ResultType.Conflict, value = null, "")
            null
        }

        // do
        launchFragmentInHiltContainer<RegisterFragment> {
            val binding = getBinding<FragmentRegisterBinding, RegisterFragment>(this, "registerBinding").apply {
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
    fun `when register succeeds, it set display to LOGIN`() {
        // Let
        val mockRegisterResult = MutableLiveData<ExecutionResult<ResponseBody>>()
        val mockDisplayResult = MutableLiveData<DisplayScreen>()
        val mockRequest = UserRegisterRequest("testEmail", "testName", "testPassword")

        // Setup Live Data
        whenever(mockRegisterViewModel.registerResult).thenReturn(mockRegisterResult)
        whenever(mockAccountViewModel.displayLiveData).thenReturn(mockDisplayResult)

        // Setup Request
        whenever(mockRegisterViewModel.requestUserRegister(mockRequest.userEmail, mockRequest.userName, mockRequest.userPassword)).thenAnswer {
            mockRegisterResult.value = ExecutionResult(ResultType.Success, value = null, "")
            null
        }

        // do
        launchFragmentInHiltContainer<RegisterFragment> {
            val binding = getBinding<FragmentRegisterBinding, RegisterFragment>(this, "registerBinding").apply {
                emailInputLayout.editText?.setText(mockRequest.userEmail)
                inputNameLayout.editText?.setText(mockRequest.userName)
                inputPasswordLayout.editText?.setText(mockRequest.userPassword)
            }

            // Click it
            binding.confirmButton.performClick().also { result ->
                Assert.assertTrue(result)
            }

            // Check it
            mockDisplayResult.getOrAwaitValue().also { result ->
                assertEquals(DisplayScreen.Login, result)
            }
        }
    }
}