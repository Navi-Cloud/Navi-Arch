package com.navi.file.repository.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.navi.file.model.ErrorResponseModel
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.UserRegisterRequest
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.factory.NaviRetrofitFactory
import com.navi.file.repository.server.user.UserRepository
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection

class UserRepositoryTest {
    private lateinit var testServer: MockWebServer
    private lateinit var userRepository: UserRepository
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val mockError: ErrorResponseModel = ErrorResponseModel(
        traceId = "",
        message = ""
    )

    @BeforeEach
    fun setupServer() {
        testServer = MockWebServer()

        // Setup User Server Repository
        NaviRetrofitFactory.createRetrofit(testServer.url(""))
        userRepository = UserRepository()
    }

    @Test
    @DisplayName("RegisterUser: RegisterUser should Conflict Type error when server responsded with conflict.")
    fun is_RegisterUser_returns_conflict_when_server_responded_conflict() {
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
    @DisplayName("RegisterUser: RegisterUser should return ok when register user succeeds.")
    fun is_RegisterUser_returns_ok_when_register_ok() {
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
    @DisplayName("LoginUser: LoginUser should return Forbidden when identity is not correct.")
    fun test() {
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
    @DisplayName("LoginUser: LoginUser should return its token when identity is correct.")
    fun is_loginUser_works_well() {
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
    @DisplayName("If one of request failed because server is offline, it should return connection-type error.")
    fun is_Request_returns_connection_error_when_server_off() {
        // Let
        testServer.shutdown()

        // Do
        val result = userRepository.loginUser(UserLoginRequest("", ""))

        // Assert
        Assert.assertEquals(ResultType.Connection, result.resultType)
    }

    @Test
    @DisplayName("If one of request's response was 'Unknown' then it should return unknown-type error.")
    fun is_request_returns_unknown_error_when_500() {
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