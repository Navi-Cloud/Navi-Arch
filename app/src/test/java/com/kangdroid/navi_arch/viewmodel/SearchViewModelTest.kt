package com.kangdroid.navi_arch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kangdroid.navi_arch.adapter.FileAdapter
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileSortingMode
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import okhttp3.HttpUrl
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import java.util.concurrent.TimeoutException

class SearchViewModelTest {
    // Target
    private lateinit var searchViewModel: SearchViewModel

    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    /* Server Setting */
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

    private val serverManagement: ServerManagement = ServerManagement.getServerManagement(
        HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(8080)
            .build()
    )

    // Mock Register Request
    private val mockUserRegisterRequest: RegisterRequest = RegisterRequest(
        userId = "user",
        userPassword = "test",
        userEmail = "Ttest",
        userName = "user"
    )

    private fun registerAndLogin() {
        serverManagement.register(mockUserRegisterRequest)
        serverManagement.loginUser(
            LoginRequest(
                userId = mockUserRegisterRequest.userId,
                userPassword = mockUserRegisterRequest.userPassword
            )
        )
    }

    /* Setting */
    @Before
    fun init() {
        serverSetup.clearData()
        searchViewModel = SearchViewModel()
        ViewModelTestHelper.setFields("serverManagement", searchViewModel, serverManagement)
    }

    @After
    fun destroy() {
        serverSetup.clearData()
    }

    /* Test */
    @Test
    fun is_search_works_well() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Make Folder
        val folderName: String = "TeST"
        serverManagement.createFolder(
            CreateFolderRequestDTO(parentFolderToken = rootToken, newFolderName = folderName)
        )

        // Perform
        searchViewModel.search(
            query = folderName,
            mode = FileSortingMode.Name,
            isReversed = false
        )

        // Assert
        searchViewModel.searchResultLiveData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it.size).isEqualTo(1)
        }
        ViewModelTestHelper.getFields<SearchViewModel, List<FileData>>("searchResultList", searchViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].fileName).isEqualTo(folderName)
        }
    }

    @Test
    fun is_search_works_well_with_default_param() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Make Folder
        val folderName: String = "TeST"
        serverManagement.createFolder(
            CreateFolderRequestDTO(parentFolderToken = rootToken, newFolderName = folderName)
        )

        // Perform
        searchViewModel.search(
            query = folderName
        )

        // Assert
        searchViewModel.searchResultLiveData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
            assertThat(it.size).isEqualTo(1)
        }
        ViewModelTestHelper.getFields<SearchViewModel, List<FileData>>("searchResultList", searchViewModel).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].fileName).isEqualTo(folderName)
        }
    }

    @Test
    fun is_search_works_well_when_server_error() {
        // Perform
        searchViewModel.search(
            query = "query"
        )

        // Assert
        searchViewModel.liveErrorData.getOrAwaitValue().also {
            assertThat(it).isNotEqualTo(null)
        }
        runCatching {
            searchViewModel.searchResultLiveData.getOrAwaitValue()
        }.onSuccess {
            Assertions.fail("This should be failed...")
        }.onFailure { throwable ->
            assertThat(throwable is TimeoutException).isEqualTo(true)
        }
    }

    private fun setListForSort() {
        val mockFileResult: List<FileData> = listOf(
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
        ViewModelTestHelper.setFields("searchResultList", searchViewModel, mockFileResult)
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_TypedName_asc() {
        setListForSort()

        // Perform
        searchViewModel.sort(
            FileSortingMode.TypedName,
            false
        )

        // Assert
        val searchResultData: List<FileData>? =
            searchViewModel.searchResultLiveData.getOrAwaitValue()

        assertThat(searchResultData).isNotEqualTo(null)
        assertThat(searchResultData!!.size).isEqualTo(2)

        // Check sorting works well
        if (searchResultData[0].fileType < searchResultData[1].fileType){
            assertThat(searchResultData[0].fileType < searchResultData[1].fileType).isEqualTo(true)
        } else if (searchResultData[0].fileName < searchResultData[1].fileName){
            assertThat(searchResultData[0].fileName < searchResultData[1].fileName).isEqualTo(true)
        } else {
            assertThat(searchResultData[0].lastModifiedTime <= searchResultData[1].lastModifiedTime).isEqualTo(true)
        }
    }

    @Test
    fun is_sort_works_when_FileSortingMode_is_TypedName_desc() {
        setListForSort()

        // Perform
        searchViewModel.sort(
            FileSortingMode.TypedName,
            true
        )

        // Assert
        val searchResultData: List<FileData>? =
            searchViewModel.searchResultLiveData.getOrAwaitValue()

        assertThat(searchResultData).isNotEqualTo(null)
        assertThat(searchResultData!!.size).isEqualTo(2)

        // Check sorting works well
        if (searchResultData[0].fileType > searchResultData[1].fileType){
            assertThat(searchResultData[0].fileType > searchResultData[1].fileType).isEqualTo(true)
        } else if (searchResultData[0].fileName > searchResultData[1].fileName){
            assertThat(searchResultData[0].fileName > searchResultData[1].fileName).isEqualTo(true)
        } else {
            assertThat(searchResultData[0].lastModifiedTime >= searchResultData[1].lastModifiedTime).isEqualTo(true)
        }
    }
}