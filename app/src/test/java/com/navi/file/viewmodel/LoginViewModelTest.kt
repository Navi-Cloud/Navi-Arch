package com.navi.file.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.user.UserRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class LoginViewModelTest: ViewModelHelper() {
    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private var mockUserRepository: UserRepository = Mockito.mock(UserRepository::class.java)
    private var loginViewModel: LoginViewModel = LoginViewModel(
        userRepository = mockUserRepository,
        dispatcherInfo = DispatcherInfo(
            uiDispatcher = Dispatchers.Unconfined,
            ioDispatcher = Dispatchers.Unconfined,
            backgroundDispatcher = Dispatchers.Unconfined
        )
    )

    @Test
    fun `Default constructor should create its instance well`() {
        val loginViewModel = LoginViewModel(mockUserRepository)
        Assert.assertNotNull(loginViewModel)
    }

    @Test
    fun `requestUserLogin should set loginResult Data well`() {
        // Setup
        val emptyRequest = UserLoginRequest("test@testemail.com", "testPasswordTesting@")
        val emptyResponse = UserLoginResponse("testToken")
        Mockito.`when`(mockUserRepository.loginUser(emptyRequest))
            .thenReturn(ExecutionResult(ResultType.Success, emptyResponse, ""))

        // Do
        loginViewModel.requestUserLogin(emptyRequest.userEmail, emptyRequest.userPassword)

        // Check
        val response = loginViewModel.loginUser.getOrAwaitValue()
        Assert.assertEquals(ResultType.Success, response.resultType)
        Assert.assertEquals(emptyResponse.userToken, response.value?.userToken)
    }

    @Test
    fun `requestUserLogin should set result as ModelValidateFailed when model is not valid`() {
        // Setup
        val emptyRequest = UserLoginRequest("test", "@")
        val emptyResponse = UserLoginResponse("testToken")

        // Do
        loginViewModel.requestUserLogin(emptyRequest.userEmail, emptyRequest.userPassword)

        // Check
        val response = loginViewModel.loginUser.getOrAwaitValue()
        Assert.assertEquals(ResultType.ModelValidateFailed, response.resultType)
    }
}