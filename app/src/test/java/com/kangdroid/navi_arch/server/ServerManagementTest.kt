package com.kangdroid.navi_arch.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kangdroid.navi_arch.ServerSetup
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.ApiError
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.*
import java.io.File
import java.net.URLEncoder

class ServerManagementTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupServer() {
            println("Setting up server..")
            ServerSetup.setupServer()
            println("Setting up server finished!")
        }

        @JvmStatic
        @AfterClass
        fun clearServer() {
            println("Clearing Server..")
            ServerSetup.clearServer(false)
            println("Clearing Server finished!")
        }
    }

    // Server Management Object
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
    @After
    fun init() {
        println("Clearing Server Data!")
        ServerSetup.clearData()
    }

    // Init Test
    @Test
    fun is_initServerCommunication_works_well() {
        assertThat(serverManagement.initWholeServerClient())
            .isEqualTo(true)
    }

    // Check whether register works well or not.
    @Test
    fun is_register_works_well() {
        serverManagement.register(mockUserRegisterRequest).also {
            assertThat(it.registeredId).isEqualTo(mockUserRegisterRequest.userId)
            assertThat(it.registeredEmail).isEqualTo(mockUserRegisterRequest.userEmail)
        }
    }

    @Test
    fun is_login_works_well() {
        serverManagement.register(mockUserRegisterRequest)

        serverManagement.loginUser(
            userLoginRequest = LoginRequest(
                userId = mockUserRegisterRequest.userId,
                userPassword = mockUserRegisterRequest.userPassword
            )
        ).also {
            assertThat(it.userToken).isNotEqualTo("")
        }
    }

    // Root Token Test
    @Test
    fun is_getRootToken_works_well() {
        registerAndLogin()
        assertThat(serverManagement.getRootToken()).isNotEqualTo("")
    }

    // Get Inside Files
    @Test
    fun is_getInsideFiles_works_well() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken
        val result: List<FileData> = serverManagement.getInsideFiles(rootToken)
        assertThat(result.size).isEqualTo(0)
    }

    private fun uploadTest() {
        val rootToken: String = serverManagement.getRootToken().rootToken
        val mockFileContents: String = "Hello, World!"
        val mockResults: String = "20"

        // Tmp File
        val file: File = File(System.getProperty("java.io.tmpdir"), "test.txt").apply {
            writeText(mockFileContents)
        }

        val requestBody : RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),file)
        val uploadFile : MultipartBody.Part = MultipartBody.Part.createFormData("uploadFile","test.txt",requestBody)
        val param : HashMap<String,Any> = HashMap()
        with(param){
            put("uploadPath", rootToken)
        }

        runCatching {
            serverManagement.upload(param, uploadFile)
        }.onFailure {
            println(it.stackTraceToString())
            fail("Something went wrong. This should be succeed.")
        }.onSuccess {
            assertThat(it).contains(rootToken)
        }

        // Cleanup
        file.delete()
    }

    private fun downloadTest() {
        // Download part
        val rootToken: String = serverManagement.getRootToken().rootToken
        val fileList: List<FileData> = serverManagement.getInsideFiles(rootToken).also {
            assertThat(it.size).isEqualTo(1)
        }

        runCatching {
            serverManagement.download(fileList[0].token, fileList[0].prevToken)
        }.onFailure {
            fail("This test should passed since we mocked our server to be succeed.")
        }.onSuccess {
            assertThat(it.fileName).isEqualTo("test.txt")
        }
    }

    @Test
    fun is_upload_download_works_well() {
        registerAndLogin()
        uploadTest()
        downloadTest()
    }
}