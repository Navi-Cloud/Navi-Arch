package com.navi.file.view.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navi.file.R
import com.navi.file.databinding.ActivityAccountBinding
import com.navi.file.helper.ViewModelFactory
import com.navi.file.hilt.ViewModelFactoryModule
import com.navi.file.model.intercommunication.DisplayScreen
import com.navi.file.view.fragment.LoginFragment
import com.navi.file.view.fragment.RegisterFragment
import com.navi.file.view.fragment.ViewModelTestHelper
import com.navi.file.viewmodel.AccountViewModel
import com.navi.file.viewmodel.LoginViewModel
import com.navi.file.viewmodel.RegisterViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import javax.inject.Singleton

@UninstallModules(ViewModelFactoryModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE, application = HiltTestApplication::class)
class AccountActivityTest: ViewModelTestHelper() {
    // AccountActivity uses AccountViewModel though, so we need to mock it.
    private lateinit var mockAccountViewModel: AccountViewModel

    // AccountActivity calls All View Model[because of fragment]
    private lateinit var mockLoginViewModel: LoginViewModel
    private lateinit var mockRegisterViewModel: RegisterViewModel

    // Application Context
    private lateinit var context: Context

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Module
    @InstallIn(SingletonComponent::class)
    inner class ViewModelFactoryTestModule {
        @Provides
        @Singleton
        fun provideTestFactory(): ViewModelFactory {
            return mock {
                on {accountViewModelFactory} doReturn(createViewModelFactory(mockAccountViewModel))
                on {loginViewModelFactory} doReturn(createViewModelFactory(mockLoginViewModel))
                on {registerViewModelFactory} doReturn(createViewModelFactory(mockRegisterViewModel))
            }
        }
    }

    @Before
    fun createInitialMock() {
        mockAccountViewModel = mock()
        mockLoginViewModel = mock()
        mockRegisterViewModel = mock()
        context = ApplicationProvider.getApplicationContext()
        hiltRule.inject()
    }

    @Test
    fun testSimple() {
        // mock LiveData
        val mockLiveData = MutableLiveData<DisplayScreen>()
        whenever(mockAccountViewModel.displayLiveData).thenReturn(mockLiveData)
        whenever(mockLoginViewModel.loginUser).thenReturn(MutableLiveData())
        whenever(mockRegisterViewModel.registerResult).thenReturn(MutableLiveData())

        val activityScenario = launchActivity<AccountActivity>().apply {
            moveToState(Lifecycle.State.RESUMED)
        }

        // Do && Assert
        activityScenario.onActivity {
            // When Default is loaded.
            it.supportFragmentManager.findFragmentById(R.id.accountViewContainer).also { tmpFragment ->
                assertTrue(tmpFragment is LoginFragment)
            }

//            // How about when user put 'register' button?
//            mockLiveData.value = DisplayScreen.Register
//            runBlocking { delay(2000) }
//            it.supportFragmentManager.findFragmentById(R.id.accountViewContainer).also { tmpFragment ->
//                assertTrue(tmpFragment is RegisterFragment)
//            }
//
//            // So user finished registering.
//            mockLiveData.value = DisplayScreen.Login
        }
    }
}