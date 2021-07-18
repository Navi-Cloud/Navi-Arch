package com.kangdroid.navi_arch.view

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.ActivityStartBinding
import com.kangdroid.navi_arch.viewmodel.PageRequest
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getFunction
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import junit.framework.TestCase
import org.assertj.core.api.Assertions
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.util.FragmentTestUtil
import org.robolectric.util.FragmentTestUtil.startFragment
import kotlin.reflect.KFunction


@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class StartActivityTest : TestCase(){

    private var context: Context? = null
    private lateinit var scenario : ActivityScenario<StartActivity>
    private var userViewModel : UserViewModel = UserViewModel()
    private val activity = Robolectric.setupActivity(StartActivity::class.java)

    @Before
    fun init(){
        context = ApplicationProvider.getApplicationContext()
    }


    @Test
    fun is_ViewBinding_well() {
        scenario = ActivityScenario.launch(StartActivity::class.java).moveToState(Lifecycle.State.CREATED)
        scenario.onActivity {
            assert(it.startBinding is ActivityStartBinding).equals(true)
        }
        scenario.close()
    }

//    @Test
//    fun is_initObserver_MAIN_well(){
//        ViewModelTestHelper.getFunction<UserViewModel>("requestMainPage")
//            .call(userViewModel)
//
//        val intent = Intent(activity,MainActivity::class.java)
//        userViewModel.pageRequest.getOrAwaitValue().also{
//            Assertions.assertThat(it).isEqualTo(PageRequest.REQUEST_MAIN)
//            val actual : Intent = shadowOf(activity).nextStartedActivity
//            print(intent.component)
//            print("SEcond"+actual.component)
//            assertEquals(intent,actual.component)
//        }
//    }

    @Test
    fun is_initObserver_LOGIN_well(){
        ViewModelTestHelper.getFunction<UserViewModel>("requestLoginPage")
            .call(userViewModel)

        userViewModel.pageRequest.getOrAwaitValue().also{
            Assertions.assertThat(it).isEqualTo(PageRequest.REQUEST_LOGIN)
            val fragmentScenario = launchFragmentInContainer<LoginFragment>(
                themeResId = R.style.Theme_NaviArch // If you don't do this, this will throw error while inflating material view
            )
            assertNotNull(fragmentScenario)
        }
    }

    @Test
    fun is_initObserver_REGISTER_well(){
        ViewModelTestHelper.getFunction<UserViewModel>("requestRegisterPage")
            .call(userViewModel)

        userViewModel.pageRequest.getOrAwaitValue().also{
            Assertions.assertThat(it).isEqualTo(PageRequest.REQUEST_REGISTER)
            val fragmentScenario = launchFragmentInContainer<RegisterFragment>(
                themeResId = R.style.Theme_NaviArch // If you don't do this, this will throw error while inflating material view
            )
            assertNotNull(fragmentScenario)
        }
    }

}