package com.kangdroid.navi_arch.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.URLEncoder

class ServerManagementTest {

    // Mock Server
    private val mockServer: MockWebServer = MockWebServer()
    private val OK: Int = 200
    private val INTERNAL_SERVER_ERROR: Int = 500
    private val baseUrl: HttpUrl by lazy {
        mockServer.url("")
    }

    // Object Mapper
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    // Server Management Object
    private val serverManagement: ServerManagement by lazy {
        ServerManagement(
            baseUrl,
            ServerManagementHelper(
                objectMapper
            )
        )
    }


    // Mock Objects
    private val mockUserToken: LoginResponse = LoginResponse("world")
    private val mockRootToken: RootTokenResponseDto = RootTokenResponseDto("hello~")
    private val mockInsideFilesResult: List<FileData> = listOf(
        FileData(
            id = 10,
            fileName = "/tmp/a.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/a.txt.token",
            lastModifiedTime = System.currentTimeMillis()
        ),
        FileData(
            id = 10,
            fileName = "/tmp/b.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/b.txt.token",
            lastModifiedTime = System.currentTimeMillis()
        ),
        FileData(
            id = 10,
            fileName = "/tmp/c.txt",
            fileType = FileType.File.toString(),
            token = "/tmp/c.txt.token",
            lastModifiedTime = System.currentTimeMillis()
        ),
        FileData(
            id = 10,
            fileName = "/tmp/test",
            fileType = FileType.Folder.toString(),
            token = "/tmp/test.token",
            lastModifiedTime = System.currentTimeMillis()
        )
    )

    private fun setDispatcherHandler(dispatcherHandler: (request: RecordedRequest) -> MockResponse ) {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return dispatcherHandler(request)
            }
        }
    }

    @Before
    fun init() {
        mockServer.start(8081)
    }

    @After
    fun destroy() {
        mockServer.shutdown()
    }

    // Init Test
    @Test
    fun is_initServerCommunication_works_well() {
        assertThat(serverManagement.initWholeServerClient())
            .isEqualTo(true)
    }

    // Root Token Test
    @Test
    fun is_getRootToken_works_well() {
        serverManagement.userToken = mockUserToken.userToken
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/root-token" -> MockResponse().setResponseCode(OK)
                    .setBody(objectMapper.writeValueAsString(mockRootToken))
                else -> fail("Ever reached target endpoint")
            }
        }
        assertThat(serverManagement.getRootToken()).isEqualTo(mockRootToken)
    }

    @Test
    fun is_getRootToken_throws_RuntimeException_500() {
        serverManagement.userToken = mockUserToken.userToken
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/root-token" -> {
                    MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
                        .setBody(
                            objectMapper.writeValueAsString(
                                ApiError(
                                    message = "Test Mocking Up",
                                    statusCode = "500",
                                    statusMessage = "Internal Server Error"
                                )
                            )
                        )
                }
                else -> fail("Ever reached target endpoint.")
            }
        }

        runCatching {
            serverManagement.getRootToken()
        }.onSuccess {
            fail("This should responded with 500 thus Runtime Exception.")
        }.onFailure {
            assertThat(it is RuntimeException).isEqualTo(true)
            assertThat(it.message).contains("Server responded with:")
        }
    }

    @Test
    fun is_getRootToken_throws_NoSuchFieldException_null_body() {
        serverManagement.userToken = mockUserToken.userToken
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/root-token" -> MockResponse().setResponseCode(OK).setBody("null")
                else -> fail("Ever reached target endpoint.")
            }
        }

        runCatching {
            serverManagement.getRootToken()
        }.onSuccess {
            fail("This should responded with 500 thus Runtime Exception.")
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is NoSuchFieldException).isEqualTo(true)
            assertThat(it.message).isEqualTo("Response was OK, but wrong response body received.")
        }
    }

    // Get Inside Files
    @Test
    fun is_getInsideFiles_works_well() {
        serverManagement.userToken = mockUserToken.userToken
        setDispatcherHandler {
            if (it.path?.contains("/api/navi/files/list/") == true) {
                MockResponse().setResponseCode(OK).setBody(
                    objectMapper.writeValueAsString(mockInsideFilesResult)
                )
            } else {
                MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
            }
        }
        val result: List<FileData> = serverManagement.getInsideFiles("rootToken")
        assertThat(result.size).isEqualTo(mockInsideFilesResult.size)
    }

    @Test
    fun is_getInsideFiles_throws_RuntimeException_500() {
        serverManagement.userToken = mockUserToken.userToken
        setDispatcherHandler {
            if (it.path?.contains("/api/navi/files/list/") == true) {
                MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
                    .setBody(
                        objectMapper.writeValueAsString(
                            ApiError(
                                message = "Test Mocking Up",
                                statusCode = "500",
                                statusMessage = "Internal Server Error"
                            )
                        )
                    )
            } else {
                fail("Test did not reached endpoint!")
            }
        }

        runCatching {
            serverManagement.getInsideFiles("rootToken")
        }.onSuccess {
            fail("We have internal server error, but request succeed?")
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is RuntimeException).isEqualTo(true)
            assertThat(it.message).contains("Server responded with:")
        }
    }

    @Test
    fun is_getInsideFiles_throws_NoSuchFieldException() {
        serverManagement.userToken = mockUserToken.userToken
        setDispatcherHandler {
            if (it.path?.contains("/api/navi/files/list/") == true) {
                MockResponse().setResponseCode(OK).setBody("null")
            } else {
                fail("Test did not reached endpoint!")
            }
        }

        runCatching {
            serverManagement.getInsideFiles("")
        }.onSuccess {
            fail("This should responded with 500 thus Runtime Exception.")
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is NoSuchFieldException).isEqualTo(true)
            assertThat(it.message).isEqualTo("Response was OK, but wrong response body received.")
        }
    }

    @Test
    fun is_upload_works_well() {
        serverManagement.userToken = mockUserToken.userToken
        val mockUploadPath: String = "somewhere_over_the_rainbow"
        val mockFileContents: String = "Hello, World!"
        val mockResults: String = "20"
        val errorMessage: String = objectMapper.writeValueAsString(
            ApiError(
                message = "Test Mocking Up",
                statusCode = "500",
                statusMessage = "Internal Server Error"
            )
        )
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/files" -> {
                    val bodyString: String = it.body.readUtf8()
                    if (it.method != "POST") {
                        println("This method should be instantiated with post method.")
                        MockResponse().setResponseCode(INTERNAL_SERVER_ERROR).setBody(errorMessage)
                    } else if (!bodyString.contains(mockUploadPath)) {
                        println("Body does not have contents: $mockUploadPath")
                        MockResponse().setResponseCode(INTERNAL_SERVER_ERROR).setBody(errorMessage)
                    } else if (!bodyString.contains(mockFileContents)) {
                        println(bodyString)
                        println("Body does not have file contents: $mockFileContents")
                        MockResponse().setResponseCode(INTERNAL_SERVER_ERROR).setBody(errorMessage)
                    } else {
                        MockResponse().setResponseCode(OK).setBody(mockResults)
                    }
                }
                else -> fail("Test did not reached endpoint!")
            }
        }

        // Tmp File
        val file: File = File(System.getProperty("java.io.tmpdir"), "test.txt").apply {
            writeText(mockFileContents)
        }

        val requestBody : RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),file)
        val uploadFile : MultipartBody.Part = MultipartBody.Part.createFormData("uploadFile","test.txt",requestBody)
        val param : HashMap<String,Any> = HashMap()
        with(param){
            put("uploadPath", mockUploadPath)
        }

        runCatching {
            serverManagement.upload(param, uploadFile)
        }.onFailure {
            println(it.stackTraceToString())
            fail("Something went wrong. This should be succeed.")
        }.onSuccess {
            assertThat(it).isEqualTo(mockResults)
        }

        // Cleanup
        file.delete()
    }

    @Test
    fun is_upload_throws_RuntimeError_500() {
        serverManagement.userToken = mockUserToken.userToken
        val mockUploadPath: String = "somewhere_over_the_rainbow"
        val mockFileContents: String = "Hello, World!"
        val mockResults: String = "20"
        val errorMessage: String = objectMapper.writeValueAsString(
            ApiError(
                message = "Test Mocking Up",
                statusCode = "500",
                statusMessage = "Internal Server Error"
            )
        )

        setDispatcherHandler {
            when (it.path) {
                "/api/navi/files" -> {
                    MockResponse().setResponseCode(INTERNAL_SERVER_ERROR).setBody(errorMessage)
                }
                else -> fail("Test did not reached endpoint!")
            }
        }

        // Tmp File
        val file: File = File(System.getProperty("java.io.tmpdir"), "test.txt").apply {
            writeText(mockFileContents)
        }

        val requestBody : RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),file)
        val uploadFile : MultipartBody.Part = MultipartBody.Part.createFormData("uploadFile","test.txt",requestBody)
        val param : HashMap<String,Any> = HashMap()
        with(param){
            put("uploadPath", mockUploadPath)
        }

        runCatching {
            serverManagement.upload(param, uploadFile)
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is RuntimeException).isEqualTo(true)
            assertThat(it.message).contains("Server responded with:")
        }.onSuccess {
            fail("This should responded with 500 thus Runtime Exception.")
        }

        // Cleanup
        file.delete()
    }

    @Test
    fun is_download_works_well() {
        serverManagement.userToken = mockUserToken.userToken
        val mockFileName: String = "TestFileName"
        val mockFileContent: String = "Whatever"
        val fileHeader: String = String.format("attachment; filename=\"%s\"", URLEncoder.encode(mockFileName, "UTF-8"))
        setDispatcherHandler {
            if (it.path?.contains("/api/navi/files/") == true) {
                MockResponse().setResponseCode(OK)
                    .setHeader("Content-Disposition", fileHeader)
                    .setBody(mockFileContent)
            } else {
                MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
            }
        }

        runCatching {
            serverManagement.download("TestToken")
        }.onFailure {
            fail("This test should passed since we mocked our server to be succeed.")
        }.onSuccess {
            assertThat(it.fileName).isEqualTo(mockFileName)
        }
    }

    @Test
    fun is_download_throws_RuntimeError_500() {
        serverManagement.userToken = mockUserToken.userToken
        val errorMessage: String = objectMapper.writeValueAsString(
            ApiError(
                message = "Test Mocking Up",
                statusCode = "500",
                statusMessage = "Internal Server Error"
            )
        )
        setDispatcherHandler {
            if (it.path?.contains("/api/navi/files/") == true) {
                MockResponse().setResponseCode(INTERNAL_SERVER_ERROR).setBody(errorMessage)
            } else {
                MockResponse().setResponseCode(OK)
            }
        }

        runCatching {
            serverManagement.download("TestToken")
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is RuntimeException).isEqualTo(true)
            assertThat(it.message).contains("Server responded with:")
        }.onSuccess {
            fail("This test should be failed since we mocked our server to be failed")
        }
    }
}