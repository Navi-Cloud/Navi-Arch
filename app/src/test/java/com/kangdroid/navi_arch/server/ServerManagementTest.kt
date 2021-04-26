package com.kangdroid.navi_arch.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import okhttp3.mockwebserver.Dispatcher
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
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
    private val serverManagement: ServerManagement = ServerManagement()
    

    // Object Mapper
    private val objectMapper: ObjectMapper = ObjectMapper()

    // Mock Objects
    private val mockRootToken: String = "RootToken"
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
        serverManagement.initServerCommunication(baseUrl.host, baseUrl.port.toString())
    }

    @After
    fun destroy() {
        mockServer.shutdown()
    }

    // Init Test
    @Test
    fun is_initServerCommunication_works_well() {
        assertThat(serverManagement.initServerCommunication(baseUrl.host, baseUrl.port.toString()))
            .isEqualTo(true)
    }

    @Test
    fun is_initServerCommunication_fails_wrong_ip() {
        assertThat(serverManagement.initServerCommunication("", ""))
            .isEqualTo(false)
    }

    // Root Token Test
    @Test
    fun is_getRootToken_works_well() {
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/rootToken" -> MockResponse().setResponseCode(OK).setBody(mockRootToken)
                else -> MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
            }
        }
        assertThat(serverManagement.getRootToken()).isEqualTo(mockRootToken)
    }

    @Test
    fun is_getRootToken_fails_internal_error() {
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/rootToken" -> MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
                else -> MockResponse().setResponseCode(OK).setBody(mockRootToken)
            }
        }
        assertThat(serverManagement.getRootToken()).isEqualTo("")
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