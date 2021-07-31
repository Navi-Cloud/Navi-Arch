package com.kangdroid.navi_arch.view

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.databinding.ActivitySearchBinding
import com.kangdroid.navi_arch.viewmodel.SearchViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class SearchActivityTest {
    // Rule that every android-thread should launched in single thread
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private inline fun<reified T> getSearchViewModel(receiver: T): SearchViewModel {
        val memberProperty = T::class.declaredMembers.find { it.name == "searchViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as SearchViewModel
    }

    private inline fun<reified T> getSearchBinding(receiver: T): ActivitySearchBinding {
        val memberProperty = T::class.declaredMembers.find { it.name == "searchBinding" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as ActivitySearchBinding
    }

    @Test
    fun is_ViewBinding_well() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            assertThat(searchBinding).isNotEqualTo(null)
        }
        scenario.close()
    }
}