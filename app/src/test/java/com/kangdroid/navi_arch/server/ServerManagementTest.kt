package com.kangdroid.navi_arch.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.data.dto.response.ApiError
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import okhttp3.mockwebserver.Dispatcher
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class ServerManagementTest {

    // Mock Server
    private val mockServer: MockWebServer = MockWebServer()
    private val OK: Int = 200
    private val INTERNAL_SERVER_ERROR: Int = 500
    private val baseUrl: HttpUrl by lazy {
        mockServer.url("")
    }

    // Server Management Object
    private val serverManagement: ServerManagement by lazy {
        ServerManagement(baseUrl)
    }
    

    // Object Mapper
    private val objectMapper: ObjectMapper = ObjectMapper()

    // Mock Objects
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
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/rootToken" -> MockResponse().setResponseCode(OK)
                    .setBody(objectMapper.writeValueAsString(mockRootToken))
                else -> fail("Ever reached target endpoint")
            }
        }
        assertThat(serverManagement.getRootToken()).isEqualTo(mockRootToken)
    }

    @Test
    fun is_getRootToken_throws_RuntimeException_500() {
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/rootToken" -> {
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
        }
    }

    @Test
    fun is_getRootToken_throws_NoSuchFieldException_wrong_body() {
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/rootToken" -> MockResponse().setResponseCode(OK).setBody("null")
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
        }
    }

    // Get Inside Files
    @Test
    fun is_getInsideFiles_works_well() {
        setDispatcherHandler {
            if (it.path?.contains("/api/navi/findInsideFiles/") == true) {
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
    fun is_getInsideFiles_fails_wrong_ip() {
        setDispatcherHandler {
            if (it.path?.contains("/api/navi/findInsideFiles/") == true) {
                MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
            } else {
                MockResponse().setResponseCode(OK).setBody(
                    objectMapper.writeValueAsString(mockInsideFilesResult)
                )
            }
        }
        val result: List<FileData> = serverManagement.getInsideFiles("rootToken")
        assertThat(result.size).isEqualTo(0)
    }
}