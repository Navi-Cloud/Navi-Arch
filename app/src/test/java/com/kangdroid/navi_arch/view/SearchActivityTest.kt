package com.kangdroid.navi_arch.view

import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.adapter.BaseFileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.databinding.ActivitySearchBinding
import com.kangdroid.navi_arch.viewmodel.SearchViewModel
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowToast
import java.util.concurrent.TimeoutException
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible


@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class SearchActivityTest {
    // Rule that every android-thread should launched in single thread
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private inline fun <reified T> getSearchViewModel(receiver: T): SearchViewModel {
        val memberProperty = T::class.declaredMembers.find { it.name == "searchViewModel" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as SearchViewModel
    }

    private inline fun <reified T> getSearchBinding(receiver: T): ActivitySearchBinding {
        val memberProperty = T::class.declaredMembers.find { it.name == "searchBinding" }!!
        memberProperty.isAccessible = true
        return memberProperty.call(receiver) as ActivitySearchBinding
    }

    private val mockFolderData: FileData = FileData(
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
    fun is_recyclerOnClickListener_works_well() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Set
            val baseFileAdapter: BaseFileAdapter = ViewModelTestHelper
                .getFields<SearchActivity, BaseFileAdapter>("baseFileAdapter", it)
            baseFileAdapter.setBaseFileList(listOf(mockFolderData))

            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            // RecyclerView needs to be measured and layed out manually in Robolectric
            searchBinding.searchResultRecyclerView.measure(0, 0);
            searchBinding.searchResultRecyclerView.layout(0, 0, 100, 10000);

            // Perform
            searchBinding.searchResultRecyclerView.getChildAt(0).performClick()

            // Assert
            assertThat(searchBinding.searchResultRecyclerView.childCount).isEqualTo(1)

            val shadowSearchActivity: ShadowActivity = shadowOf(it)
            val targetIntent: Intent = Intent(it, MainActivity::class.java)
            assertThat(shadowSearchActivity.nextStartedActivity.component).isEqualTo(targetIntent.component)
        }
        scenario.close()
    }

    @Test
    fun is_inputSearch_works_well_when_KEYCODE_ENTER() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.apply {
                // Perform
                inputSearch.dispatchKeyEvent(KeyEvent(
                    KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_ENTER
                ))

                // Assert
                assertThat(progressBar.visibility).isEqualTo(View.VISIBLE)

                // TODO Assert liveErrorData
                // [Using TestCoroutineDispatcher] This throw RuntimeException: Can't toast on a thread that has not called Looper.prepare()
                // Same Error with LoginFragmentTest.loginBtn_is_fail()
//                val searchViewModel: SearchViewModel = getSearchViewModel(it)
//                // ENTER will change liveErrorData since Server not exists now
//                searchViewModel.liveErrorData.getOrAwaitValue().also { list ->
//                    assertThat(list).isNotEqualTo(null)
//                }
            }
        }
        scenario.close()
    }

    @Test
    fun is_inputSearch_works_well_when_other_keycode() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.apply {
                // Perform
                inputSearch.dispatchKeyEvent(KeyEvent(
                    KeyEvent.ACTION_UP, KeyEvent.ACTION_DOWN
                ))

                // Assert
                assertThat(progressBar.visibility).isEqualTo(View.GONE)

                val searchViewModel: SearchViewModel = getSearchViewModel(it)
                runCatching {
                    searchViewModel.liveErrorData.getOrAwaitValue()
                }.onSuccess {
                    Assertions.fail("This should be failed...")
                }.onFailure { throwable ->
                    assertThat(throwable is TimeoutException).isEqualTo(true)
                }
            }
            scenario.close()
        }
    }

    @Test
    fun is_init_searchResultRecyclerView_well() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.searchResultRecyclerView.apply {
                assertThat(layoutManager!!::class.java).isEqualTo(LinearLayoutManager::class.java)
                val baseFileAdapter: BaseFileAdapter = ViewModelTestHelper
                    .getFields<SearchActivity, BaseFileAdapter>("baseFileAdapter", it)
                assertThat(adapter).isEqualTo(baseFileAdapter)
            }
        }
        scenario.close()
    }

    @Test
    fun is_backButton_works_well() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.backButton.performClick()

            assertThat(it.isFinishing).isEqualTo(true)
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

    @Test
    fun is_liveErrorData_observer_works_well() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Perform
            val testErrorMsg: String = "erROR"
            val searchViewModel: SearchViewModel = getSearchViewModel(it)
            searchViewModel.liveErrorData.value = RuntimeException(testErrorMsg)

            // Assert
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.apply {
                assertThat(progressBar.visibility).isEqualTo(View.GONE)
                assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Search Error: $testErrorMsg")
            }
        }
        scenario.close()
    }

    @Test
    fun is_liveErrorData_observer_works_well_when_null() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Perform
            val searchViewModel: SearchViewModel = getSearchViewModel(it)
            searchViewModel.liveErrorData.value = null

            // Assert
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.apply {
                assertThat(progressBar.visibility).isEqualTo(View.GONE)
            }
        }
        scenario.close()
    }

    @Test
    fun is_toggleButton_sortByNameOrLMT_works_well() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Perform
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.sortByNameOrLMT.performClick()

            // Assert
            val searchViewModel: SearchViewModel = getSearchViewModel(it)
            searchViewModel.searchResultLiveData.getOrAwaitValue().also { list ->
                assertThat(list).isNotEqualTo(null)
            }
            ViewModelTestHelper.getFields<SearchActivity, FileSortingMode>(
                "currentSortMode", it
            ).also { fileSortingMode ->
                assertThat(fileSortingMode).isEqualTo(FileSortingMode.TypedLMT)
            }
        }
        scenario.close()
    }

    @Test
    fun is_toggleButton_sortByType_works_well() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Perform
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.sortByType.performClick()

            // Assert
            val searchViewModel: SearchViewModel = getSearchViewModel(it)
            searchViewModel.searchResultLiveData.getOrAwaitValue().also { list ->
                assertThat(list).isNotEqualTo(null)
            }
            ViewModelTestHelper.getFields<SearchActivity, FileSortingMode>(
                "currentSortMode", it
            ).also { fileSortingMode ->
                assertThat(fileSortingMode).isEqualTo(FileSortingMode.Name)
            }
        }
        scenario.close()
    }

    @Test
    fun is_toggleButton_sortByAscendingOrDescending_works_well() {
        val scenario = ActivityScenario
            .launch(SearchActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)

        scenario.onActivity {
            // Perform
            val searchBinding: ActivitySearchBinding = getSearchBinding(it)
            searchBinding.sortByAscendingOrDescending.performClick()

            // Assert
            val searchViewModel: SearchViewModel = getSearchViewModel(it)
            searchViewModel.searchResultLiveData.getOrAwaitValue().also { list ->
                assertThat(list).isNotEqualTo(null)
            }
            ViewModelTestHelper.getFields<SearchActivity, Boolean>(
                "isReversed", it
            ).also { isReversed ->
                assertThat(isReversed).isEqualTo(true)
            }
        }
        scenario.close()
    }
}