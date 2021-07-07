package com.kangdroid.navi_arch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.utils.PagerCacheUtils
import okhttp3.HttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.After
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

    // Mock User
    private val mockUserId: String = "je"

    // Mock Server
    private val mockServer: MockWebServer = MockWebServer()
    private val OK: Int = 200
    private val INTERNAL_SERVER_ERROR: Int = 500
    private val baseUrl: HttpUrl by lazy {
        mockServer.url("")
    }

    // Object Mapper
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Before
    fun init() {
        mockServer.start(8082)
    }

    @After
    fun destroy() {
        mockServer.shutdown()
    }

    private fun initServerManagement() {
        val serverManagement: ServerManagement by lazy {
            ServerManagement(
                baseUrl
            )
        }
        serverManagement.userToken = "UserToken"
        setFields("serverManagement", serverManagement)

        // innerExplorePage needs getInsideFiles() from server
        setDispatcherHandler {
            when {
                it.path == "/api/navi/root-token" -> MockResponse().setResponseCode(OK).setBody(
                    objectMapper.writeValueAsString(RootTokenResponseDto(mockRootToken)))
                it.path!!.contains("/api/navi/files/list/") -> MockResponse().setResponseCode(OK).setBody(
                    objectMapper.writeValueAsString(mockInsideFilesResult)
                )
                else -> MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
            }
        }
    }

    private fun setDispatcherHandler(dispatcherHandler: (request: RecordedRequest) -> MockResponse ) {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return dispatcherHandler(request)
            }
        }
    }

    private fun set_PageSet_List_Cache(targetPrevToken: String, targetFileAdapter: FileAdapter) {
        getFields<MutableSet<String>>("pageSet").add(targetPrevToken)
        getFields<MutableList<FileAdapter>>("pageList").add(targetFileAdapter)
        getFields<PagerCacheUtils>("pagerCacheUtils").createCache(
            targetPrevToken,
            targetFileAdapter
        )
    }

    // Fake data
    private val fakePrevToken: String = "/tmp.token"
    private val mockRootToken: String = "RootToken"
    private val mockInsideFilesResult: List<FileData> = listOf(
        FileData(
            userId = mockUserId,
            fileName = "/tmp/a.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/a.txt.token",
            prevToken = fakePrevToken
        ),
        FileData(
            userId = mockUserId,
            fileName = "/tmp/b.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/b.txt.token",
            prevToken = fakePrevToken
        ),
    )
    private val fakeFileAdapter: FileAdapter = FileAdapter(
        onClick = { _, _ -> },
        onLongClick = { true },
        fileList = mockInsideFilesResult,
        pageNumber = 10,
        currentFolder = FileData(
            userId = mockUserId,
            fileName = "/tmp",
            fileType = FileType.File.toString(),
            token = fakePrevToken,
            prevToken = "root"
        )
    )

    @Before
    fun createViewModel() {
        pagerViewModel = PagerViewModel(PagerCacheUtils())
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
        assertThat(pageList[0].fileList.size).isEqualTo(2)
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
        assertThat(pageList[0].fileList.size).isEqualTo(2)
    }

    @Test
    fun is_innerExplorePage_works_well_non_root() {
        // Mock Server init
        initServerManagement()

        getFunction("innerExplorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserId,
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = "TestToken",
                    prevToken = "TestPrevToken"
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
        assertThat(pageList[0].fileList).isEqualTo(mockInsideFilesResult)
    }

    @Test
    fun is_innerExplorePage_works_well_root() {
        // Mock Server init
        initServerManagement()

        getFunction("innerExplorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserId,
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = "TestToken",
                    prevToken = "TestPrevToken"
                ),
                true
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
        assertThat(pageSet.contains(mockRootToken)).isEqualTo(true)
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.fileName).isEqualTo("testFileName")
        assertThat(pageList[0].fileList).isEqualTo(mockInsideFilesResult)
    }

    @Test
    fun is_innerExplorePage_throw_error_when_fail_getRootToken() {
        // Mock Server init
        initServerManagement()
        setDispatcherHandler {  MockResponse().setResponseCode(INTERNAL_SERVER_ERROR) }

        getFunction("innerExplorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserId,
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = "TestToken",
                    prevToken = "TestPrevToken"
                ),
                true
            )

        // Get Live Data
        val liveErrorData: Throwable? =
            pagerViewModel.liveErrorData.getOrAwaitValue()

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        assertThat(liveErrorData).isNotEqualTo(null)
        assertThat(pageSet.size).isEqualTo(0)
        assertThat(pageList.size).isEqualTo(0)
    }

    @Test
    fun is_innerExplorePage_throw_error_when_fail_getInsideFiles() {
        // Mock Server init
        initServerManagement()
        setDispatcherHandler {  MockResponse().setResponseCode(INTERNAL_SERVER_ERROR) }

        getFunction("innerExplorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserId,
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = "TestToken",
                    prevToken = "TestPrevToken"
                ),
                true
            )

        // Get Live Data
        val liveErrorData: Throwable? =
            pagerViewModel.liveErrorData.getOrAwaitValue()

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        assertThat(liveErrorData).isNotEqualTo(null)
        assertThat(pageSet.size).isEqualTo(0)
        assertThat(pageList.size).isEqualTo(0)
    }

    @Test
    fun is_explorePage_works_when_cleanUp_true_with_no_cache() {
        initServerManagement()

        // explorePage with cleanUp = true
        getFunction("explorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserId,
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = fakePrevToken,
                    prevToken = "TestPrevToken"
                ),
                10,
                true) // cleanUp = true

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)
        assertThat(pageSet.contains(fakePrevToken)).isEqualTo(true)
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageSet.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.fileName).isEqualTo("testFileName")
        assertThat(pageList[0].fileList).isEqualTo(mockInsideFilesResult)
    }

    @Test
    fun is_explorePage_works_when_cleanUp_true_with_cache() {
        initServerManagement()

        // Set data first: token = fakePrevToken, fileAdapter = fakeFileAdapter
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // explorePage with cleanUp = true
        getFunction("explorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserId,
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = fakePrevToken,
                    prevToken = "TestPrevToken"
                ),
                10,
                true) // cleanUp = true

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)
        assertThat(pageSet.contains(fakePrevToken)).isEqualTo(true)
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageSet.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.fileName).isEqualTo("testFileName")
        assertThat(pageList[0].fileList).isEqualTo(mockInsideFilesResult)
    }

    @Test
    fun is_explorePage_works_when_cleanUp_false_with_no_cache() {
        initServerManagement()

        // explorePage with cleanUp = false
        getFunction("explorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserId,
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = fakePrevToken,
                    prevToken = "TestPrevToken"
                ),
                10,
                false) // cleanUp = false

        // Get Live Data will NOT be fail since page cache hasn't FileData
        runCatching {
            pagerViewModel.livePagerData.getOrAwaitValue()
        }.onSuccess {
            assertThat(it).isNotEqualTo(null)
            assertThat(it!!.size).isEqualTo(1)
        }.onFailure {
            fail("This Should be success...")
        }

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        assertThat(pageSet.contains(fakePrevToken)).isEqualTo(true)
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageSet.size).isEqualTo(1)

        // This will be "testFileName" (Because there aren't any data with fakePrevToken)
        assertThat(pageList[0].currentFolder.fileName).isEqualTo("testFileName")
        assertThat(pageList[0].fileList).isEqualTo(mockInsideFilesResult)
    }

    @Test
    fun is_explorePage_works_when_cleanUp_false_with_cache() {
        initServerManagement()

        // Set data first: token = fakePrevToken, fileAdapter = fakeFileAdapter
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // explorePage with cleanUp = false
        getFunction("explorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserId,
                    fileName = "testFileName",
                    fileType = "Folder",
                    token = fakePrevToken,
                    prevToken = "TestPrevToken"
                ),
                10,
                false) // cleanUp = false

        // Get Live Data will be fail since page cache has FileData (no clean up)
        runCatching {
            pagerViewModel.livePagerData.getOrAwaitValue()
        }.onSuccess {
            fail("This Should be fail...")
        }.onFailure {
            assertThat(it is TimeoutException).isEqualTo(true)
        }

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet")

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList")

        assertThat(pageSet.contains(fakePrevToken)).isEqualTo(true)
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageSet.size).isEqualTo(1)

        // This will be fakeFileAdapter.currentFolder.fileName, not "testFileName" (Because it doesn't clean up)
        assertThat(pageList[0].currentFolder.fileName).isEqualTo(fakeFileAdapter.currentFolder.fileName)
        assertThat(pageList[0].fileList).isEqualTo(mockInsideFilesResult)
    }

    @Test
    fun is_createInitialRootPage_works_well() {
        // Mock Server init
        initServerManagement()

        getFunction("createInitialRootPage")
            .call(
                pagerViewModel
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
        assertThat(pageSet.contains(mockRootToken)).isEqualTo(true)
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.fileName).isEqualTo("/")
        assertThat(pageList[0].fileList).isEqualTo(mockInsideFilesResult)
    }

    @Test
    fun is_sortList_works_well_normal() {
        val mockInsideFilesResult: List<FileData> = listOf(
            FileData(
                userId = mockUserId,
                fileName = "b",
                fileType = FileType.File.toString(),
                token = "/tmp/a.txt.token",
                prevToken = fakePrevToken
            ),
            FileData(
                userId = mockUserId,
                fileName = "a",
                fileType = FileType.File.toString(),
                token = "/tmp/b.txt.token",
                prevToken = fakePrevToken
            ),
            FileData(
                userId = mockUserId,
                fileName = "c",
                fileType = FileType.File.toString(),
                token = "/tmp/c.txt.token",
                prevToken = fakePrevToken
            ),
            FileData(
                userId = mockUserId,
                fileName = "0",
                fileType = FileType.Folder.toString(),
                token = "/tmp/test.token",
                prevToken = fakePrevToken
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
                userId = mockUserId,
                fileName = "b",
                fileType = FileType.File.toString(),
                token = "/tmp/a.txt.token",
                prevToken = fakePrevToken
            ),
            FileData(
                userId = mockUserId,
                fileName = "a",
                fileType = FileType.File.toString(),
                token = "/tmp/b.txt.token",
                prevToken = fakePrevToken
            ),
            FileData(
                userId = mockUserId,
                fileName = "c",
                fileType = FileType.File.toString(),
                token = "/tmp/c.txt.token",
                prevToken = fakePrevToken
            ),
            FileData(
                userId = mockUserId,
                fileName = "0",
                fileType = FileType.Folder.toString(),
                token = "/tmp/test.token",
                prevToken = fakePrevToken
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

    @Test
    fun is_sort_works_when_FileSortingMode_is_TypedName_asc() {
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // do
        val requestPageNum: Int = 0
        getFunction("sort")
            .call(
                pagerViewModel,
                FileSortingMode.TypedName,
                false, // reverse = false
                requestPageNum
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Check sorting works well
        val targetFileAdapter: FileAdapter = livePagerData[requestPageNum]
        val targetFileList: List<FileData> = targetFileAdapter.fileList

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo(fakePrevToken)
        assertThat(targetFileList.size).isEqualTo(2)
        if (targetFileList[0].fileType < targetFileList[1].fileType){
            assertThat(targetFileList[0].fileType < targetFileList[1].fileType).isEqualTo(true)
        } else if (targetFileList[0].fileName < targetFileList[1].fileName){
            assertThat(targetFileList[0].fileName < targetFileList[1].fileName).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].lastModifiedTime <= targetFileList[1].lastModifiedTime).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_TypedName_desc() {
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // do
        val requestPageNum: Int = 0
        getFunction("sort")
            .call(
                pagerViewModel,
                FileSortingMode.TypedName,
                true, // reverse = true
                requestPageNum
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Check sorting works well
        val targetFileAdapter: FileAdapter = livePagerData[requestPageNum]
        val targetFileList: List<FileData> = targetFileAdapter.fileList

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo(fakePrevToken)
        assertThat(targetFileList.size).isEqualTo(2)
        if (targetFileList[0].fileType > targetFileList[1].fileType){
            assertThat(targetFileList[0].fileType > targetFileList[1].fileType).isEqualTo(true)
        } else if (targetFileList[0].fileName > targetFileList[1].fileName){
            assertThat(targetFileList[0].fileName > targetFileList[1].fileName).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].lastModifiedTime >= targetFileList[1].lastModifiedTime).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_TypedLMT_asc() {
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // do
        val requestPageNum: Int = 0
        getFunction("sort")
            .call(
                pagerViewModel,
                FileSortingMode.TypedLMT,
                false, // reverse = false
                requestPageNum
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Check sorting works well
        val targetFileAdapter: FileAdapter = livePagerData[requestPageNum]
        val targetFileList: List<FileData> = targetFileAdapter.fileList

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo(fakePrevToken)
        if (targetFileList[0].fileType < targetFileList[1].fileType){
            assertThat(targetFileList[0].fileType < targetFileList[1].fileType).isEqualTo(true)
        } else if (targetFileList[0].lastModifiedTime < targetFileList[1].lastModifiedTime){
            assertThat(targetFileList[0].lastModifiedTime < targetFileList[1].lastModifiedTime).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].fileName <= targetFileList[1].fileName).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_TypedLMT_desc() {
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // do
        val requestPageNum: Int = 0
        getFunction("sort")
            .call(
                pagerViewModel,
                FileSortingMode.TypedLMT,
                true, // reverse = true
                requestPageNum
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Check sorting works well
        val targetFileAdapter: FileAdapter = livePagerData[requestPageNum]
        val targetFileList: List<FileData> = targetFileAdapter.fileList

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo(fakePrevToken)
        assertThat(targetFileList.size).isEqualTo(2)
        if (targetFileList[0].fileType > targetFileList[1].fileType){
            assertThat(targetFileList[0].fileType > targetFileList[1].fileType).isEqualTo(true)
        } else if (targetFileList[0].lastModifiedTime > targetFileList[1].lastModifiedTime){
            assertThat(targetFileList[0].lastModifiedTime > targetFileList[1].lastModifiedTime).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].fileName >= targetFileList[1].fileName).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_Name_asc() {
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // do
        val requestPageNum: Int = 0
        getFunction("sort")
            .call(
                pagerViewModel,
                FileSortingMode.Name,
                false, // reverse = false
                requestPageNum
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)


        // Check sorting works well
        val targetFileAdapter: FileAdapter = livePagerData[requestPageNum]
        val targetFileList: List<FileData> = targetFileAdapter.fileList

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo(fakePrevToken)
        if (targetFileList[0].fileName < targetFileList[1].fileName) {
            assertThat(targetFileList[0].fileName < targetFileList[1].fileName).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].lastModifiedTime <= targetFileList[1].lastModifiedTime).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_Name_desc() {
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // do
        val requestPageNum: Int = 0
        getFunction("sort")
            .call(
                pagerViewModel,
                FileSortingMode.Name,
                true, // reverse = true
                requestPageNum
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Check sorting works well
        val targetFileAdapter: FileAdapter = livePagerData[requestPageNum]
        val targetFileList: List<FileData> = targetFileAdapter.fileList

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo(fakePrevToken)
        assertThat(targetFileList.size).isEqualTo(2)
        if (targetFileList[0].fileName > targetFileList[1].fileName){
            assertThat(targetFileList[0].fileName > targetFileList[1].fileName).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].lastModifiedTime >= targetFileList[1].lastModifiedTime).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_LMT_asc() {
        fakeFileAdapter.fileList[0].lastModifiedTime = 2000
        fakeFileAdapter.fileList[1].lastModifiedTime = 1000
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // do
        val requestPageNum: Int = 0
        getFunction("sort")
            .call(
                pagerViewModel,
                FileSortingMode.LMT,
                false, // reverse = false
                requestPageNum
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Check sorting works well
        val targetFileAdapter: FileAdapter = livePagerData[requestPageNum]
        val targetFileList: List<FileData> = targetFileAdapter.fileList

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo(fakePrevToken)
        if (targetFileList[0].lastModifiedTime < targetFileList[1].lastModifiedTime){
            assertThat(targetFileList[0].lastModifiedTime < targetFileList[1].lastModifiedTime).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].fileName <= targetFileList[1].fileName).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_LMT_desc() {
        fakeFileAdapter.fileList[0].lastModifiedTime = 2000
        fakeFileAdapter.fileList[1].lastModifiedTime = 1000
        set_PageSet_List_Cache(fakePrevToken, fakeFileAdapter)

        // do
        val requestPageNum: Int = 0
        getFunction("sort")
            .call(
                pagerViewModel,
                FileSortingMode.LMT,
                true, // reverse = true
                requestPageNum
            )

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Check sorting works well
        val targetFileAdapter: FileAdapter = livePagerData[requestPageNum]
        val targetFileList: List<FileData> = targetFileAdapter.fileList

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo(fakePrevToken)
        assertThat(targetFileList.size).isEqualTo(2)
        if (targetFileList[0].lastModifiedTime > targetFileList[1].lastModifiedTime){
            assertThat(targetFileList[0].lastModifiedTime > targetFileList[1].lastModifiedTime).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].fileName >= targetFileList[1].fileName).isEqualTo(true)
        }
    }
}
