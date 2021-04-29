package com.kangdroid.navi_arch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.utils.PagerCacheUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.reflect.Field
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

class PagerViewModelTest {

    // Fake Server
    private val fakeServerManagement: FakeServerManagement = FakeServerManagement()

    // Target
    private lateinit var pagerViewModel: PagerViewModel

    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    /* Copyright 2019 Google LLC.
   SPDX-License-Identifier: Apache-2.0 */
    fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data = o
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }

        this.observeForever(observer)

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }

    private fun<T> getFields(fieldName: String): T {
        // Get Page Set
        val targetField: Field = PagerViewModel::class.java.getDeclaredField(fieldName).apply {
            isAccessible = true
        }

        @Suppress("UNCHECKED_CAST")
        return targetField.get(pagerViewModel) as T
    }

    private fun<T> setFields(fieldName: String, targetObject: T) {
        PagerViewModel::class.java.getDeclaredField(fieldName).apply {
            isAccessible = true
            set(pagerViewModel, targetObject)
        }
    }

    private fun getFunction(functionName: String): KFunction<*> {
        // Private Method testing
        return PagerViewModel::class.declaredMemberFunctions.find {
            it.name == functionName
        }!!.apply {
            isAccessible = true
        }
    }

    // Fake adapter
    private val fakeFileAdapter: FileAdapter = FileAdapter(
        onClick = { _, _ -> },
        onLongClick = { true },
        fileList = listOf(
            FileData(
                id = 10,
                fileName = "/tmp/a.txt",
                fileType = FileType.File.toString(),
                token = "/tmp/a.txt.token",
                lastModifiedTime = System.currentTimeMillis()
            ),
        ),
        pageNumber = 10,
        currentFolder = FileData(
            id = 1,
            fileName = "/tmp",
            fileType = FileType.File.toString(),
            token = "/tmp.token",
            lastModifiedTime = System.currentTimeMillis()
        )
    )

    @Before
    fun createViewModel() {
        pagerViewModel = PagerViewModel(fakeServerManagement, PagerCacheUtils())
    }

    @Test
    fun is_updatePageAndNotify_works_well_no_cache() {
        // Private Method testing
        getFunction("updatePageAndNotify")
            .call(pagerViewModel, fakeFileAdapter, "test_token", false)

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        // Live Set Test
        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page Set Test
        assertThat(pageSet.size).isEqualTo(1)
        assertThat(pageSet.contains("test_token")).isEqualTo(true)

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        // Page List Test
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.token).isEqualTo("/tmp.token")
        assertThat(pageList[0].fileList.size).isEqualTo(1)
    }

    @Test
    fun is_updatePageAndNotify_works_well_with_cache() {
        getFunction("updatePageAndNotify")
            .call(pagerViewModel, fakeFileAdapter, "test_token", true)

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        // Live Set Test
        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page Set Test
        assertThat(pageSet.size).isEqualTo(1)
        assertThat(pageSet.contains("test_token")).isEqualTo(true)

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        // Page List Test
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.token).isEqualTo("/tmp.token")
        assertThat(pageList[0].fileList.size).isEqualTo(1)
    }

    @Test
    fun is_innerExplorePage_works_well_non_root() {
        getFunction("innerExplorePage")
            .call(
                pagerViewModel,
                FileData(
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = "TestToken",
                    lastModifiedTime = System.currentTimeMillis()
                ),
                false
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)
        assertThat(pageSet.contains("TestToken")).isEqualTo(true)
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.fileName).isEqualTo("testFileName")
    }


    @Test
    fun is_sortList_works_well_normal() {
        val mockInsideFilesResult: List<FileData> = listOf(
            FileData(
                id = 10,
                fileName = "b",
                fileType = FileType.File.toString(),
                token = "/tmp/a.txt.token",
                lastModifiedTime = System.currentTimeMillis()
            ),
            FileData(
                id = 10,
                fileName = "a",
                fileType = FileType.File.toString(),
                token = "/tmp/b.txt.token",
                lastModifiedTime = System.currentTimeMillis()
            ),
            FileData(
                id = 10,
                fileName = "c",
                fileType = FileType.File.toString(),
                token = "/tmp/c.txt.token",
                lastModifiedTime = System.currentTimeMillis()
            ),
            FileData(
                id = 10,
                fileName = "0",
                fileType = FileType.Folder.toString(),
                token = "/tmp/test.token",
                lastModifiedTime = System.currentTimeMillis()
            )
        )

        val sortedList: List<FileData> = getFunction("sortList").call(
            pagerViewModel,
            mockInsideFilesResult
        ) as List<FileData>

        assertThat(sortedList.size).isEqualTo(mockInsideFilesResult.size)
        assertThat(sortedList[0].fileName).isEqualTo(mockInsideFilesResult[1].fileName)
        assertThat(sortedList[sortedList.size-1].fileName).isEqualTo(mockInsideFilesResult[mockInsideFilesResult.size-1].fileName)
    }

    @Test
    fun is_sortList_works_well_reversed() {
        val mockInsideFilesResult: List<FileData> = listOf(
            FileData(
                id = 10,
                fileName = "b",
                fileType = FileType.File.toString(),
                token = "/tmp/a.txt.token",
                lastModifiedTime = System.currentTimeMillis()
            ),
            FileData(
                id = 10,
                fileName = "a",
                fileType = FileType.File.toString(),
                token = "/tmp/b.txt.token",
                lastModifiedTime = System.currentTimeMillis()
            ),
            FileData(
                id = 10,
                fileName = "c",
                fileType = FileType.File.toString(),
                token = "/tmp/c.txt.token",
                lastModifiedTime = System.currentTimeMillis()
            ),
            FileData(
                id = 10,
                fileName = "0",
                fileType = FileType.Folder.toString(),
                token = "/tmp/test.token",
                lastModifiedTime = System.currentTimeMillis()
            )
        )

        val sortedList: List<FileData> = getFunction("sortList").call(
            pagerViewModel,
            mockInsideFilesResult
        ) as List<FileData>

        assertThat(sortedList.size).isEqualTo(mockInsideFilesResult.size)
        assertThat(sortedList[sortedList.size-1].fileName).isEqualTo(mockInsideFilesResult[mockInsideFilesResult.size-1].fileName)
        assertThat(sortedList[0].fileName).isEqualTo(mockInsideFilesResult[1].fileName)
    }

    @Test
    fun is_removePrecedingPages_calculates_correctly() {
        val mockRequestPageNumber: Int = 1
        val mockPageList: MutableList<FileAdapter> = mutableListOf(
            fakeFileAdapter,
            fakeFileAdapter,
            fakeFileAdapter
        )

        // Set
        setFields("pageList", mockPageList)

        // do
        getFunction("removePrecedingPages")
            .call(
                pagerViewModel,
                mockRequestPageNumber
            )

        // Get Results
        val resultList: List<FileAdapter> = getFields("pageList")

        // Assert
        assertThat(resultList.size).isEqualTo(1)
    }

    @Test
    fun is_removePrecedingPages_calculates_correctly_size_smaller() {
        // do
        getFunction("removePrecedingPages")
            .call(
                pagerViewModel,
                10
            )

        // Get Results
        val resultList: List<FileAdapter> = getFields("pageList")

        // Assert
        assertThat(resultList.size).isEqualTo(0)
    }
}