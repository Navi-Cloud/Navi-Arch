package com.kangdroid.navi_arch.view

import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.FragmentRegisterBinding
import org.junit.runner.RunWith
import org.assertj.core.api.Assertions.assertThat
import com.kangdroid.navi_arch.viewmodel.UserViewModel
import org.junit.*
import org.robolectric.annotation.Config
import androidx.lifecycle.Lifecycle.State
import com.kangdroid.navi_arch.databinding.FragmentLoginBinding

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {

    private lateinit var userViewModel: UserViewModel

    @Before
    fun init() {
        userViewModel = UserViewModel()
    }

    @Test
    fun is_viewBinding_ok() {
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch // If you don't do this, this will throw error while inflating material view
        )
        scenario.moveToState(State.CREATED)
        scenario.onFragment{
            assertThat(it.loginBinding is FragmentLoginBinding?).isEqualTo(true)
            assertThat(it).isNotEqualTo(null)
        }
    }

    @Test
    fun is_onDestroyView_works_well(){
        val scenario = launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_NaviArch
        )
        scenario.onFragment {
            it.onDestroyView()
            assertThat(it.loginBinding is FragmentLoginBinding?).isEqualTo(true)
            assertThat(userViewModel.loginErrorData.value).isEqualTo(null)
            assertThat(it.loginBinding).isEqualTo(null)
        }
        scenario.moveToState(State.DESTROYED)
    }

}