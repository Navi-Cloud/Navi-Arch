package com.kangdroid.navi_arch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.utils.PagerCacheUtils
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getFields
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getFunction
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.setFields
import okhttp3.HttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.*
import java.util.concurrent.TimeoutException
import kotlin.reflect.KFunction

class PagerViewModelTest {

    companion object {
        @JvmStatic
        val serverSetup: ServerSetup = ServerSetup(
            if (System.getProperty("os.name").contains("Windows")) {
                WindowsServerSetup()
            } else {
                LinuxServerSetup()
            }
        )

        @JvmStatic
        @BeforeClass
        fun setupServer() {
            println("Setting up server..")
            serverSetup.setupServer()
            println("Setting up server finished!")
        }

        @JvmStatic
        @AfterClass
        fun clearServer() {
            println("Clearing Server..")
            serverSetup.killServer(false)
            println("Clearing Server finished!")
        }
    }

    // Target
    private lateinit var pagerViewModel: PagerViewModel

    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val serverManagement: ServerManagement = ServerManagement.getServerManagement(
        HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(8080)
            .build()
    )

    // Mock Register Request
    private val mockUserRegisterRequest: RegisterRequest = RegisterRequest(
        userId = "kangdroid",
        userPassword = "test",
        userEmail = "Ttest",
        userName = "KangDroid"
    )

    private val fakeFileAdapter: FileAdapter = FileAdapter(
        onClick = { _, _ -> },
        onLongClick = { true },
        fileList = listOf(),
        pageNumber = 10,
        currentFolder = FileData(
            userId = mockUserRegisterRequest.userId,
            fileName = "/tmp",
            fileType = FileType.File.toString(),
            token = "",
            prevToken = "root"
        )
    )

    private val mockInsideFilesResult: List<FileData> = listOf(
        FileData(
            userId = "mockUserId",
            fileName = "/tmp/a.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/a.txt.token",
            prevToken = "fakePrevToken"
        ),
        FileData(
            userId = "mockUserId",
            fileName = "/tmp/b.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/b.txt.token",
            prevToken = "fakePrevToken"
        ),
    )

    private fun set_PageSet_List_Cache(targetPrevToken: String, targetFileAdapter: FileAdapter) {
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).add(targetPrevToken)
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).add(targetFileAdapter)
        getFields<PagerViewModel, PagerCacheUtils>("pagerCacheUtils", pagerViewModel).createCache(
            targetPrevToken,
            targetFileAdapter
        )
    }

    private fun registerAndLogin() {
        serverManagement.register(mockUserRegisterRequest)
        serverManagement.loginUser(
            LoginRequest(
                userId = mockUserRegisterRequest.userId,
                userPassword = mockUserRegisterRequest.userPassword
            )
        )
    }

    @Before
    fun init() {
        serverSetup.clearData()
        pagerViewModel = PagerViewModel()
        ViewModelTestHelper.setFields("serverManagement", pagerViewModel, serverManagement)
    }

    @After
    fun destroy() {
        serverSetup.clearData()
    }
    @Test
    fun is_updatePageAndNotify_works_well_default_param() {
        // Private Method testing with default parameters -> use callBy() instead of call()
        val funUpdatePageAndNotify: KFunction<*> = getFunction<PagerViewModel>("updatePageAndNotify")
        val adapterParam = funUpdatePageAndNotify.parameters.first { it.name == "fileAdapter" }
        val tokenParam = funUpdatePageAndNotify.parameters.first { it.name == "targetToken" }

        funUpdatePageAndNotify.callBy(mapOf(
            funUpdatePageAndNotify.parameters[0] to pagerViewModel,
            adapterParam to fakeFileAdapter,
            tokenParam to "test_token"
        ))

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        // Live Set Test
        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet", pagerViewModel)

        // Page Set Test
        assertThat(pageSet.size).isEqualTo(1)
        assertThat(pageSet.contains("test_token")).isEqualTo(true)

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList", pagerViewModel)

        // Page List Test
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.token).isEqualTo("")
        assertThat(pageList[0].fileList.size).isEqualTo(0)
    }

    @Test
    fun is_updatePageAndNotify_works_well_no_cache() {
        // Private Method testing
        getFunction<PagerViewModel>("updatePageAndNotify")
            .call(pagerViewModel, fakeFileAdapter, "test_token", false)

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        // Live Set Test
        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet", pagerViewModel)

        // Page Set Test
        assertThat(pageSet.size).isEqualTo(1)
        assertThat(pageSet.contains("test_token")).isEqualTo(true)

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList", pagerViewModel)

        // Page List Test
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.token).isEqualTo("")
        assertThat(pageList[0].fileList.size).isEqualTo(0)
    }

    @Test
    fun is_updatePageAndNotify_works_well_with_cache() {
        getFunction<PagerViewModel>("updatePageAndNotify")
            .call(pagerViewModel, fakeFileAdapter, "test_token", true)

        // Get Live Data
        val livePagerData: MutableList<FileAdapter>? =
            pagerViewModel.livePagerData.getOrAwaitValue()

        // Live Set Test
        assertThat(livePagerData).isNotEqualTo(null)
        assertThat(livePagerData!!.size).isEqualTo(1)

        // Get Value for set
        val pageSet: MutableSet<String> = getFields("pageSet", pagerViewModel)

        // Page Set Test
        assertThat(pageSet.size).isEqualTo(1)
        assertThat(pageSet.contains("test_token")).isEqualTo(true)

        // Page List
        val pageList: MutableList<FileAdapter> = getFields("pageList", pagerViewModel)

        // Page List Test
        assertThat(pageList.size).isEqualTo(1)
        assertThat(pageList[0].currentFolder.token).isEqualTo("")
        assertThat(pageList[0].fileList.size).isEqualTo(0)
    }

    @Test
    fun is_innerExplorePage_works_well_root() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        getFunction<PagerViewModel>("innerExplorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserRegisterRequest.userId,
                    fileName = "/",
                    fileType = "Folder",
                    token = "TestToken",
                    prevToken = "TestPrevToken"
                ),
                true
            )

        // Get Live Data
        pagerViewModel.livePagerData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it.size).isEqualTo(1)
        }

        // Get Value for set
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).also {
            assertThat(it.contains(rootToken)).isEqualTo(true)
        }

        // Page List
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].currentFolder.fileName).isEqualTo("/")
            assertThat(it[0].fileList.size).isEqualTo(0)
        }
    }

    @Test
    fun is_innerExplorePage_throw_error_when_getRootToken_fail() {
        // No Register & Login -> fail to getRootToken()
        // Check innerExplorePage() works well when isRoot=true but fail to getRootToken()
        getFunction<PagerViewModel>("innerExplorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserRegisterRequest.userId,
                    fileName = "/",
                    fileType = "Folder",
                    token = "TestToken",
                    prevToken = "TestPrevToken"
                ),
                true
            )

        // Get Live Data [Error]
        pagerViewModel.liveErrorData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
        }
    }

    @Test
    fun is_innerExplorePage_throw_error_when_getInsideFiles_fail() {
        // No Register & Login -> fail to getInsideFiles()
        // Check innerExplorePage() works well when isRoot=false and fail to getInsideFiles()
        getFunction<PagerViewModel>("innerExplorePage")
            .call(
                pagerViewModel,
                FileData(
                    userId = mockUserRegisterRequest.userId,
                    fileName = "/",
                    fileType = "Folder",
                    token = "TestToken",
                    prevToken = "TestPrevToken"
                ),
                false
            )

        // Get Live Data [Error]
        pagerViewModel.liveErrorData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
        }
    }

    @Test
    fun is_explorePage_works_when_cleanUp_true_with_no_cache() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // explorePage with cleanUp = true
        pagerViewModel.explorePage(
            nextFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                token = rootToken,
                prevToken = ""
            ),
            0,
            true
        )

        // Get Live Data
        pagerViewModel.livePagerData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it.size).isEqualTo(1)
        }


        // Get Value for set
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).also {
            assertThat(it.contains(rootToken)).isEqualTo(true)
            assertThat(it.size).isEqualTo(1)
        }

        // Page List
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].currentFolder.token).isEqualTo(rootToken)
            assertThat(it[0].fileList.size).isEqualTo(0)
        }
    }

    @Test
    fun is_explorePage_works_when_cleanUp_true_with_cache() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken
        val mockFileAdapter: FileAdapter = FileAdapter(
            onClick = {_, _ -> },
            onLongClick = {true},
            fileList = listOf(),
            currentFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                prevToken = "",
                token = rootToken
            ),
            pageNumber = 0
        )

        // Set data first: token = fakePrevToken, fileAdapter = fakeFileAdapter
        setFields("pageList", pagerViewModel, mutableListOf(mockFileAdapter))
        setFields("pageSet", pagerViewModel, mutableSetOf(rootToken))
        val pagerCacheUtils: PagerCacheUtils = PagerCacheUtils().apply {
            createCache(rootToken, mockFileAdapter)
        }
        setFields("pagerCacheUtils", pagerViewModel, pagerCacheUtils)

        // explorePage with cleanUp = true
        pagerViewModel.explorePage(
            nextFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                prevToken = "",
                token = rootToken
            ),
            requestedPageNumber = 0,
            cleanUp = true
        )

        // Get Live Data
        pagerViewModel.livePagerData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it.size).isEqualTo(1)
        }

        // Page Set
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).also {
            assertThat(it.contains(rootToken)).isEqualTo(true)
        }

        // Page List
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].currentFolder.fileName).isEqualTo("/")
            assertThat(it[0].fileList.size).isEqualTo(0)
        }
    }

    @Test
    fun is_explorePage_works_when_cleanUp_false_with_no_cache() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        pagerViewModel.explorePage(
            nextFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                token = rootToken,
                prevToken = ""
            ),
            0,
            false
        )

        // Get Live Data will NOT be fail since page cache hasn't FileData
        pagerViewModel.livePagerData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it!!.size).isEqualTo(1)
        }

        // Get Value for set
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).also {
            assertThat(it.contains(rootToken)).isEqualTo(true)
        }

        // Page List
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].currentFolder.fileName).isEqualTo("/")
            assertThat(it[0].fileList.size).isEqualTo(0)
        }
    }

    @Test
    fun is_explorePage_works_when_cleanUp_false_with_cache() {
        registerAndLogin()

        val rootToken: String = serverManagement.getRootToken().rootToken
        val mockFileAdapter: FileAdapter = FileAdapter(
            onClick = {_, _ -> },
            onLongClick = {true},
            fileList = listOf(),
            currentFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                prevToken = "",
                token = rootToken
            ),
            pageNumber = 0
        )

        // Set data first: token = fakePrevToken, fileAdapter = fakeFileAdapter
        setFields("pageList", pagerViewModel, mutableListOf(mockFileAdapter))
        setFields("pageSet", pagerViewModel, mutableSetOf(rootToken))
        val pagerCacheUtils: PagerCacheUtils = PagerCacheUtils().apply {
            createCache(rootToken, mockFileAdapter)
        }
        setFields("pagerCacheUtils", pagerViewModel, pagerCacheUtils)

        // explorePage with cleanUp = false
        pagerViewModel.explorePage(
            nextFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                token = rootToken,
                prevToken = ""
            ),
            0, // removePrecedingPages() will remove this data
            false
        )

        // Get Live Data will NOT be fail since page cache hasn't FileData
        pagerViewModel.livePagerData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it!!.size).isEqualTo(1)
        }

        // Get Value for set
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).also {
            assertThat(it.contains(rootToken)).isEqualTo(true)
        }

        // Page List
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].currentFolder.fileName).isEqualTo("/")
            assertThat(it[0].fileList.size).isEqualTo(0)
        }
    }

    @Test
    fun is_explorePage_works_when_cleanUp_false_with_cache_and_pageSet() {
        registerAndLogin()

        val rootToken: String = serverManagement.getRootToken().rootToken
        val mockFileAdapter: FileAdapter = FileAdapter(
            onClick = {_, _ -> },
            onLongClick = {true},
            fileList = listOf(),
            currentFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                prevToken = "",
                token = rootToken
            ),
            pageNumber = 0
        )

        // Set data first: token = fakePrevToken, fileAdapter = fakeFileAdapter
        setFields("pageList", pagerViewModel, mutableListOf(mockFileAdapter))
        setFields("pageSet", pagerViewModel, mutableSetOf(rootToken))
        val pagerCacheUtils: PagerCacheUtils = PagerCacheUtils().apply {
            createCache(rootToken, mockFileAdapter)
        }
        setFields("pagerCacheUtils", pagerViewModel, pagerCacheUtils)

        // explorePage with cleanUp = false
        pagerViewModel.explorePage(
            nextFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                token = rootToken,
                prevToken = ""
            ),
            10, // removePrecedingPages() will NOT remove this data
            false
        )

        // Get Live Data
        // Since there're already have "rootToken" folder data and cleanUp=false, livePagerData never set
        runCatching {
            pagerViewModel.livePagerData.getOrAwaitValue()
        }.onSuccess {
            fail("This should be failed...")
        }.onFailure {
            // fail to get livedata
            assertThat(it is TimeoutException).isEqualTo(true)
        }

        // Get Value for set
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).also {
            assertThat(it.contains(rootToken)).isEqualTo(true)
        }

        // Page List
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].currentFolder.fileName).isEqualTo("/")
            assertThat(it[0].fileList.size).isEqualTo(0)
        }
    }

    @Test
    fun is_explorePage_works_when_cleanUp_false_with_default_param() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        pagerViewModel.explorePage(
            nextFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                token = rootToken,
                prevToken = ""
            ),
            0
        )

        // Get Live Data will NOT be fail since page cache hasn't FileData
        pagerViewModel.livePagerData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it!!.size).isEqualTo(1)
        }

        // Get Value for set
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).also {
            assertThat(it.contains(rootToken)).isEqualTo(true)
        }

        // Page List
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].currentFolder.fileName).isEqualTo("/")
            assertThat(it[0].fileList.size).isEqualTo(0)
        }
    }


    @Test
    fun is_createInitialRootPage_works_well() {
        // Mock Server init
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Create Initial Root Page
        pagerViewModel.createInitialRootPage()

        // Get Live Data
        pagerViewModel.livePagerData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it.size).isEqualTo(1)
        }

        // Get Value for set
        getFields<PagerViewModel, MutableSet<String>>("pageSet", pagerViewModel).also {
            assertThat(it.contains(rootToken)).isEqualTo(true)
        }

        // Page List
        getFields<PagerViewModel, MutableList<FileAdapter>>("pageList", pagerViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].currentFolder.fileName).isEqualTo("/")
            assertThat(it[0].fileList.size).isEqualTo(0)
        }
    }

    @Test
    fun is_sortList_works_well_normal() {
        val mockInsideFilesResult: List<FileData> = listOf(
            FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "b",
                fileType = FileType.File.toString(),
                token = "/tmp/a.txt.token",
                prevToken = ""
            ),
            FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "a",
                fileType = FileType.File.toString(),
                token = "/tmp/b.txt.token",
                prevToken = ""
            ),
            FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "c",
                fileType = FileType.File.toString(),
                token = "/tmp/c.txt.token",
                prevToken = ""
            ),
            FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "0",
                fileType = FileType.Folder.toString(),
                token = "/tmp/test.token",
                prevToken = ""
            )
        )

        val sortedList: List<FileData> = getFunction<PagerViewModel>("sortList").call(
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
                userId = "mockUserId",
                fileName = "b",
                fileType = FileType.File.toString(),
                token = "/tmp/a.txt.token",
                prevToken = "fakePrevToken"
            ),
            FileData(
                userId = "mockUserId",
                fileName = "a",
                fileType = FileType.File.toString(),
                token = "/tmp/b.txt.token",
                prevToken = "fakePrevToken"
            ),
            FileData(
                userId = "mockUserId",
                fileName = "c",
                fileType = FileType.File.toString(),
                token = "/tmp/c.txt.token",
                prevToken = "fakePrevToken"
            ),
            FileData(
                userId = "mockUserId",
                fileName = "0",
                fileType = FileType.Folder.toString(),
                token = "/tmp/test.token",
                prevToken = "fakePrevToken"
            )
        )

        val sortedList: List<FileData> = getFunction<PagerViewModel>("sortList").call(
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
        setFields("pageList", pagerViewModel, mockPageList)

        // do
        getFunction<PagerViewModel>("removePrecedingPages")
            .call(
                pagerViewModel,
                mockRequestPageNumber
            )

        // Get Results
        val resultList: List<FileAdapter> = getFields("pageList", pagerViewModel)

        // Assert
        assertThat(resultList.size).isEqualTo(1)
    }

    @Test
    fun is_removePrecedingPages_calculates_correctly_size_smaller() {
        // do
        getFunction<PagerViewModel>("removePrecedingPages")
            .call(
                pagerViewModel,
                10
            )

        // Get Results
        val resultList: List<FileAdapter> = getFields("pageList", pagerViewModel)

        // Assert
        assertThat(resultList.size).isEqualTo(0)
    }

    private fun setListForSort() {
        val mockFileAdapter: FileAdapter = FileAdapter(
            onClick = {_, _ -> },
            onLongClick = {true},
            fileList = mockInsideFilesResult,
            currentFolder = FileData(
                userId = mockUserRegisterRequest.userId,
                fileName = "/",
                fileType = FileType.Folder.toString(),
                prevToken = "",
                token = "rootToken"
            ),
            pageNumber = 0
        )
        setFields("pageList", pagerViewModel, mutableListOf(mockFileAdapter))
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_TypedName_asc() {
        setListForSort()

        // do
        val requestPageNum: Int = 0
        pagerViewModel.sort(
            FileSortingMode.TypedName,
            false,
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

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo("rootToken")
        assertThat(targetFileList.size).isEqualTo(mockInsideFilesResult.size)
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
        setListForSort()

        // do
        val requestPageNum: Int = 0
        pagerViewModel.sort(
            FileSortingMode.TypedName,
            true,
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

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo("rootToken")
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
        setListForSort()

        // do
        val requestPageNum: Int = 0
        pagerViewModel.sort(
            FileSortingMode.TypedLMT,
            false,
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

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo("rootToken")
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
        setListForSort()

        // do
        val requestPageNum: Int = 0
        pagerViewModel.sort(
            FileSortingMode.TypedLMT,
            true,
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

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo("rootToken")
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
        setListForSort()

        // do
        val requestPageNum: Int = 0
        pagerViewModel.sort(
            FileSortingMode.Name,
            false,
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

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo("rootToken")
        if (targetFileList[0].fileName < targetFileList[1].fileName) {
            assertThat(targetFileList[0].fileName < targetFileList[1].fileName).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].lastModifiedTime <= targetFileList[1].lastModifiedTime).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_Name_desc() {
        setListForSort()

        // do
        val requestPageNum: Int = 0
        pagerViewModel.sort(
            FileSortingMode.Name,
            true,
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

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo("rootToken")
        assertThat(targetFileList.size).isEqualTo(2)
        if (targetFileList[0].fileName > targetFileList[1].fileName){
            assertThat(targetFileList[0].fileName > targetFileList[1].fileName).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].lastModifiedTime >= targetFileList[1].lastModifiedTime).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_LMT_asc() {
        mockInsideFilesResult[0].lastModifiedTime = 2000
        mockInsideFilesResult[1].lastModifiedTime = 1000
        setListForSort()

        // do
        val requestPageNum: Int = 0
        pagerViewModel.sort(
            FileSortingMode.LMT,
            false,
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

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo("rootToken")
        if (targetFileList[0].lastModifiedTime < targetFileList[1].lastModifiedTime){
            assertThat(targetFileList[0].lastModifiedTime < targetFileList[1].lastModifiedTime).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].fileName <= targetFileList[1].fileName).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_LMT_desc() {
        mockInsideFilesResult[0].lastModifiedTime = 2000
        mockInsideFilesResult[1].lastModifiedTime = 1000
        setListForSort()

        // do
        val requestPageNum: Int = 0
        pagerViewModel.sort(
            FileSortingMode.LMT,
            true,
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

        assertThat(targetFileAdapter.currentFolder.token).isEqualTo("rootToken")
        assertThat(targetFileList.size).isEqualTo(2)
        if (targetFileList[0].lastModifiedTime > targetFileList[1].lastModifiedTime){
            assertThat(targetFileList[0].lastModifiedTime > targetFileList[1].lastModifiedTime).isEqualTo(true)
        } else {
            assertThat(targetFileList[0].fileName >= targetFileList[1].fileName).isEqualTo(true)
        }
    }

    @Test
    fun is_recyclerOnClickListener_works_well_when_folder() {
        registerAndLogin()

        val recyclerOnClickListener: ((FileData, Int) -> Unit) =
            getFields("recyclerOnClickListener", pagerViewModel)
        val fileData: FileData = FileData(
            userId = mockUserRegisterRequest.userId,
            fileName = "testFileName",
            fileType = "Folder",
            token = "TestToken",
            prevToken = "TestPrevToken"
        )
        recyclerOnClickListener(fileData, 10)

        // Get Live Data
        // Since FileData is "Folder", recyclerOnClickListener should call explorePage() and inner func update livePagerData
        runCatching {
            pagerViewModel.livePagerData.getOrAwaitValue()
        }.onSuccess {
            assertThat(it).isNotEqualTo(null)
            assertThat(it.size).isEqualTo(1)
        }.onFailure {
            fail("This should be succeed...")
        }
    }

    @Test
    fun is_recyclerOnClickListener_works_well_when_file() {
        registerAndLogin()

        val recyclerOnClickListener: ((FileData, Int) -> Unit) =
            getFields("recyclerOnClickListener", pagerViewModel)
        val fileData: FileData = FileData(
            userId = mockUserRegisterRequest.userId,
            fileName = "testFileName",
            fileType = "File",
            token = "TestToken",
            prevToken = "TestPrevToken"
        )
        recyclerOnClickListener(fileData, 10)

        // Get Live Data
        // Since FileData is "File", recyclerOnClickListener don't call explorePage()
        runCatching {
            pagerViewModel.livePagerData.getOrAwaitValue()
        }.onSuccess {
            fail("This should be failed...")
        }.onFailure {
            assertThat(it is TimeoutException).isEqualTo(true)
        }
    }

//    @Test
//    fun is_innerExplorePage_works_well_non_root() {
//        registerAndLogin()
//        val rootToken: String = serverManagement.getRootToken().rootToken
//
//        // Create Folder on root - ahhhh we don't have folder creation api right..?
//
//        getFunction<PagerViewModel>("innerExplorePage")
//            .call(
//                pagerViewModel,
//                FileData(
//                    userId = mockUserRegisterRequest.userId,
//                    fileName = "testFileName",
//                    fileType = "Folder",
//                    token = "TestToken",
//                    prevToken = "TestPrevToken"
//                ),
//                false
//            )
//
//        // Get Live Data
//        val livePagerData: MutableList<FileAdapter>? =
//            pagerViewModel.livePagerData.getOrAwaitValue()
//
//        // Get Value for set
//        val pageSet: MutableSet<String> = getFields("pageSet")
//
//        // Page List
//        val pageList: MutableList<FileAdapter> = getFields("pageList")
//
//        assertThat(livePagerData).isNotEqualTo(null)
//        assertThat(livePagerData!!.size).isEqualTo(1)
//        assertThat(pageSet.contains("TestToken")).isEqualTo(true)
//        assertThat(pageList.size).isEqualTo(1)
//        assertThat(pageList[0].currentFolder.fileName).isEqualTo("testFileName")
//        assertThat(pageList[0].fileList).isEqualTo(mockInsideFilesResult)
//    }
//

//    @Test
//    fun is_innerExplorePage_throw_error_when_fail_getInsideFiles() {
//        // Mock Server init
//        initServerManagement()
//        setDispatcherHandler {  MockResponse().setResponseCode(INTERNAL_SERVER_ERROR) }
//
//        getFunction("innerExplorePage")
//            .call(
//                pagerViewModel,
//                FileData(
//                    userId = mockUserId,
//                    fileName = "testFileName",
//                    fileType = "Folder",
//                    token = "TestToken",
//                    prevToken = "TestPrevToken"
//                ),
//                true
//            )
//
//        // Get Live Data
//        val liveErrorData: Throwable? =
//            pagerViewModel.liveErrorData.getOrAwaitValue()
//
//        // Get Value for set
//        val pageSet: MutableSet<String> = getFields("pageSet")
//
//        // Page List
//        val pageList: MutableList<FileAdapter> = getFields("pageList")
//
//        assertThat(liveErrorData).isNotEqualTo(null)
//        assertThat(pageSet.size).isEqualTo(0)
//        assertThat(pageList.size).isEqualTo(0)
//    }
}
