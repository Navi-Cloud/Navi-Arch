package com.kangdroid.navi_arch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import okhttp3.HttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.*

class MenuBottomSheetViewModelTest {
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
    private lateinit var menuBottomSheetViewModel: MenuBottomSheetViewModel

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

    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        serverSetup.clearData()
        menuBottomSheetViewModel = MenuBottomSheetViewModel()
        ViewModelTestHelper.setFields("serverManagement", menuBottomSheetViewModel, serverManagement)
    }

    @After
    fun destroy() {
        serverSetup.clearData()
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

    @Test
    fun is_createFolder_works_well() {
        registerAndLogin()

        // Get rootToken
        val rootToken: String = serverManagement.getRootToken().rootToken

        // To request
        val mockCreateFolderRequest: CreateFolderRequestDTO = CreateFolderRequestDTO(
            parentFolderToken = rootToken,
            newFolderName = "TestFolder"
        )

        // Do
        menuBottomSheetViewModel.createFolder(mockCreateFolderRequest)

        menuBottomSheetViewModel.createFolderResult.getOrAwaitValue().also {
            assertThat(it.isSucceed).isEqualTo(true)
        }
    }

    @Test
    fun is_createFolder_fails_well() {
        // To request
        val mockCreateFolderRequest: CreateFolderRequestDTO = CreateFolderRequestDTO(
            parentFolderToken = "rootToken",
            newFolderName = "TestFolder"
        )

        // Try to create another folder
        menuBottomSheetViewModel.createFolder(mockCreateFolderRequest)

        menuBottomSheetViewModel.createFolderResult.getOrAwaitValue().also {
            assertThat(it.isSucceed).isEqualTo(false)
        }
    }
}