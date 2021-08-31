package com.navi.file.viewmodel

import com.navi.file.InstantExecutorExtension
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.UserRegisterRequest
import com.navi.file.repository.server.ExecutionResult
import com.navi.file.repository.server.ResultType
import com.navi.file.repository.server.ServerRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Assert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*

@ExtendWith(InstantExecutorExtension::class)
class UserViewModelTest: ViewModelHelper() {
    private var mockServerRepository: ServerRepository = mock(ServerRepository::class.java)
    private var userViewModel: UserViewModel = UserViewModel(
        serverRepository = mockServerRepository,
        dispatcherInfo = DispatcherInfo(
            uiDispatcher = Dispatchers.Unconfined,
            ioDispatcher = Dispatchers.Unconfined,
            backgroundDispatcher = Dispatchers.Unconfined
        )
    )

    @Test
    @DisplayName("requestUserRegister should set registerResult Data well.")
    fun is_requestUserRegister_works_Well() {
        // Setup
        val emptyRequest = UserRegisterRequest("", "", "")
        `when`(mockServerRepository.registerUser(emptyRequest))
            .thenReturn(ExecutionResult(ResultType.Success, null, ""))

        // Do
        userViewModel.requestUserRegister(emptyRequest)

        // Check
        val response = userViewModel.registerResult.getOrAwaitValue()
        Assert.assertEquals(ResultType.Success, response.resultType)
    }

    @Test
    @DisplayName("requestUserLogin: requestUserLogin should set loginResult Data well.")
    fun is_requestUserLogin_works_well() {
        // Setup
        val emptyRequest = UserLoginRequest("", "")
        val emptyResponse = UserLoginResponse("testToken")
        `when`(mockServerRepository.loginUser(emptyRequest))
            .thenReturn(ExecutionResult(ResultType.Success, emptyResponse, ""))

        // Do
        userViewModel.requestUserLogin(emptyRequest)

        // Check
        val response = userViewModel.loginUser  .getOrAwaitValue()
        Assert.assertEquals(ResultType.Success, response.resultType)
        Assert.assertEquals(emptyResponse.userToken, response.value?.userToken)
    }
}