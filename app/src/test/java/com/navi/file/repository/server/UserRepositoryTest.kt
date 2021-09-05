package com.navi.file.repository.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.navi.file.model.ErrorResponseModel
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.UserRegisterRequest
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.user.UserRepository
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class UserRepositoryTest {
    private lateinit var testServer: MockWebServer
    private lateinit var userRepository: UserRepository
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val mockError: ErrorResponseModel = ErrorResponseModel(
        traceId = "",
        message = ""
    )

    @Before
    fun setupServer() {
        testServer = MockWebServer()

        // Setup User Server Repository
        NaviRetrofitFactory.createRetrofit(testServer.url(""))
        userRepository = UserRepository
    }

    @Test
    fun `RegisterUser should Conflict Type error when server responsded with conflict`() {
        // Setup Server
        testServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_CONFLICT)
                .setBody(objectMapper.writeValueAsString(mockError))
        )
        val result = userRepository.registerUser(UserRegisterRequest("", "", ""))

        // Assert
        Assert.assertEquals(ResultType.Conflict, result.resultType)
    }

    @Test
    fun `RegisterUser should return ok when register user succeeds`() {
        // Setup Server
        testServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )
        val result = userRepository.registerUser(UserRegisterRequest("", "", ""))

        // Assert
        Assert.assertEquals(ResultType.Success, result.resultType)
    }

    @Test
    fun `LoginUser should return Forbidden when identity is not correct`() {
        // Setup Server
        testServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
                .setBody(objectMapper.writeValueAsString(mockError))
        )
        val result = userRepository.loginUser(UserLoginRequest("", ""))

        // Assert
        Assert.assertEquals(ResultType.Forbidden, result.resultType)
    }

    @Test
    fun `LoginUser should return its token when identity is correct`() {
        // Setup Server
        testServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(objectMapper.writeValueAsString(UserLoginResponse("testToken")))
        )

        // Do
        val result = userRepository.loginUser(UserLoginRequest("", ""))

        // Assert
        Assert.assertEquals(ResultType.Success, result.resultType)
        Assert.assertNotNull(result.value)
        Assert.assertEquals("testToken", result.value?.userToken)
    }

    /**
     * Private Method Testing from here.
     */
    @Test
    fun `If one of request failed because server is offline, it should return connection-type error`() {
        // Let
        testServer.shutdown()

        // Do
        val result = userRepository.loginUser(UserLoginRequest("", ""))

        // Assert
        Assert.assertEquals(ResultType.Connection, result.resultType)
    }

    @Test
    fun `If one of request's response was 'Unknown' then it should return unknown-type error`() {
        // Let
        testServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        )

        // Do
        val result = userRepository.loginUser(UserLoginRequest("", ""))

        // Assert
        Assert.assertEquals(ResultType.Unknown, result.resultType)
    }

}