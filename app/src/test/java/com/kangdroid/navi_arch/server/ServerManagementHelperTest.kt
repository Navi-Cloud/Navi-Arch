package com.kangdroid.navi_arch.server

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kangdroid.navi_arch.data.dto.response.ApiError
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.EOFException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ServerManagementHelperTest {
    // Mock Server
    private val mockServer: MockWebServer = MockWebServer()
    private val OK: Int = 200
    private val INTERNAL_SERVER_ERROR: Int = 500
    private val baseUrl: HttpUrl by lazy {
        mockServer.url("")
    }

    // Mock Object
    private val mockUserToken: LoginResponse = LoginResponse("world")
    private val mockRootToken: RootTokenResponseDto = RootTokenResponseDto("hello~")
    private val mockHeaders: HashMap<String, Any?> = HashMap()
    init {
        mockHeaders["X-AUTH-TOKEN"] = mockUserToken.userToken
    }

    // Mock Retrofit[API]
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .writeTimeout(5000, TimeUnit.MILLISECONDS)
        .build()

    private val api: APIInterface by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIInterface::class.java)
    }

    // Object Mapper
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private fun setDispatcherHandler(dispatcherHandler: (request: RecordedRequest) -> MockResponse) {
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

    @Test
    fun is_exchangeDataWithServer_throws_network_error_no_server() {
        runCatching {
            ServerManagementHelper.exchangeDataWithServer(api.getRootToken(mockHeaders))
        }.onSuccess {
            fail("exchangeDataWithServer should fail because server is OFF now.")
        }.onFailure {
            assertThat(it is SocketTimeoutException).isEqualTo(true)
            println(it.stackTraceToString())
        }
    }

    @Test
    fun is_exchangeDataWithServer_throws_IOFException_no_body() {
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/root-token" -> MockResponse().setResponseCode(OK)
                else -> fail("Did not reached endpoint.")
            }
        }

        runCatching {
            ServerManagementHelper.exchangeDataWithServer(api.getRootToken(mockHeaders))
        }.onSuccess {
            fail("This should not be fine because")
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is EOFException).isEqualTo(true)
        }
    }

    @Test
    fun is_exchangeDataWithServer_ok() {
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/root-token" -> MockResponse().setResponseCode(OK).setBody(
                    objectMapper.writeValueAsString(mockRootToken)
                )
                else -> fail("Did not reached endpoint.")
            }
        }

        runCatching {
            ServerManagementHelper.exchangeDataWithServer(api.getRootToken(mockHeaders))
        }.onSuccess {
            assertThat(it.code()).isEqualTo(OK)
        }.onFailure {
            println(it.stackTraceToString())
            fail("This should not throw thing since this is OK test")
        }
    }

    @Test
    fun is_handleDataError_throws_NoSuchFieldException_wrong_body() {
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/root-token" -> MockResponse().setResponseCode(INTERNAL_SERVER_ERROR).setBody(
                    "objectMapper.writeValueAsString(mockRootToken)"
                )
                else -> fail("Did not reached endpoint.")
            }
        }

        runCatching {
            ServerManagementHelper.handleDataError(ServerManagementHelper.exchangeDataWithServer(api.getRootToken(mockHeaders)))
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is JsonParseException).isEqualTo(true)
        }.onSuccess {
            fail("This should result in fail, because we are using wrong json string.")
        }
    }

    @Test
    fun is_handleDataError_throws_RuntimeException() {
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/root-token" -> MockResponse().setResponseCode(INTERNAL_SERVER_ERROR).setBody(
                    objectMapper.writeValueAsString(
                        ApiError(
                            message = "Mock Error",
                            statusCode = "500",
                            statusMessage = "Internal Server Error"
                        )
                    )
                )
                else -> fail("Did not reached endpoint.")
            }
        }

        runCatching {
            ServerManagementHelper.handleDataError(ServerManagementHelper.exchangeDataWithServer(api.getRootToken(mockHeaders)))
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is RuntimeException).isEqualTo(true)
        }.onSuccess {
            fail("This should result in fail, because we are using wrong json string.")
        }
    }

    @Test
    fun is_handleDataError_throws_NoSuchFieldException_no_message_field() {
        data class TestingObject(
            var name: String = "Mock"
        )
        setDispatcherHandler {
            when (it.path) {
                "/api/navi/root-token" -> MockResponse().setResponseCode(INTERNAL_SERVER_ERROR).setBody(
                    objectMapper.writeValueAsString(
                        TestingObject()
                    )
                )
                else -> fail("Did not reached endpoint.")
            }
        }

        runCatching {
            ServerManagementHelper.handleDataError(ServerManagementHelper.exchangeDataWithServer(api.getRootToken(mockHeaders)))
        }.onFailure {
            println(it.stackTraceToString())
            assertThat(it is NoSuchFieldException).isEqualTo(true)
        }.onSuccess {
            fail("This should result in fail, because we are using wrong json string.")
        }
    }
}