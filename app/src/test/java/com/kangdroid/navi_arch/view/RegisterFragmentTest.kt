package com.kangdroid.navi_arch.view

import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class RegisterFragmentTest {
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
}