package com.kangdroid.navi_arch.view

import android.os.Build
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.adapter.BaseFileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.databinding.ActivitySearchBinding
import com.kangdroid.navi_arch.viewmodel.SearchViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
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

    val mockFolderData: FileData = FileData(
        userId = "id",
        token = "token",
        prevToken = "prev",
        fileName = "name",
        fileType = "Folder"
    )

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

    @Test
    fun is_searchResultLiveData_observer_works_well_when_list_is_empty() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Perform
            val searchViewModel: SearchViewModel = getSearchViewModel(it)
            searchViewModel.searchResultLiveData.value = listOf()

            // Assert
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.apply {
                assertThat(progressBar.visibility).isEqualTo(View.GONE)
                assertThat(searchResultRecyclerView.visibility).isEqualTo(View.GONE)
                assertThat(textNoResult.visibility).isEqualTo(View.VISIBLE)
            }
        }
        scenario.close()
    }

    @Test
    fun is_searchResultLiveData_observer_works_well_when_list_is_not_empty() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Perform
            val searchViewModel: SearchViewModel = getSearchViewModel(it)
            searchViewModel.searchResultLiveData.value = listOf(mockFolderData)

            // Assert
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.apply {
                assertThat(progressBar.visibility).isEqualTo(View.GONE)
                assertThat(searchResultRecyclerView.visibility).isEqualTo(View.VISIBLE)
                assertThat(textNoResult.visibility).isEqualTo(View.GONE)
            }
            ViewModelTestHelper.getFields<SearchActivity, BaseFileAdapter>(
                "baseFileAdapter", it
            ).also { baseFileAdapter ->
                assertThat(baseFileAdapter.fileList.size).isEqualTo(1)
                assertThat(baseFileAdapter.fileList[0].fileName).isEqualTo(mockFolderData.fileName)
            }
        }
        scenario.close()
    }

    @Test
    fun is_searchResultLiveData_observer_works_well_when_null() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Perform
            val searchViewModel: SearchViewModel = getSearchViewModel(it)
            searchViewModel.searchResultLiveData.value = null

            // Assert
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.apply {
                assertThat(progressBar.visibility).isEqualTo(View.GONE)
            }
        }
        scenario.close()
    }
}