package com.navi.file.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.navi.file.model.UserLoginRequest
import com.navi.file.model.UserLoginResponse
import com.navi.file.model.UserRegisterRequest
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.user.UserRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class UserViewModelTest: ViewModelHelper() {
    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private var mockUserRepository: UserRepository = mock(UserRepository::class.java)
    private var userViewModel: UserViewModel = UserViewModel(
        userRepository = mockUserRepository,
        dispatcherInfo = DispatcherInfo(
            uiDispatcher = Dispatchers.Unconfined,
            ioDispatcher = Dispatchers.Unconfined,
            backgroundDispatcher = Dispatchers.Unconfined
        )
    )

    @Test
    fun `Default constructor should create its instance well`() {
        val userViewModel = UserViewModel(mockUserRepository)
        Assert.assertNotNull(userViewModel)
    }

    @Test
    fun `requestUserRegister should set registerResult Data well`() {
        // Setup
        val emptyRequest = UserRegisterRequest("kangdroid@testwhatever.com", "", "asdfasdf!@#$")
        `when`(mockUserRepository.registerUser(emptyRequest))
            .thenReturn(ExecutionResult(ResultType.Success, null, ""))

        // Do
        userViewModel.requestUserRegister(emptyRequest)

        // Check
        val response = userViewModel.registerResult.getOrAwaitValue()
        Assert.assertEquals(ResultType.Success, response.resultType)
    }

    @Test
    fun `requestUserRegister should return ModelValidateFailed when model is not valid`() {
        // Setup
        val emptyRequest = UserRegisterRequest("kangdroid@", "", "!")

        // Do
        userViewModel.requestUserRegister(emptyRequest)

        // Check
        val response = userViewModel.registerResult.getOrAwaitValue()
        Assert.assertEquals(ResultType.ModelValidateFailed, response.resultType)
    }

    @Test
    fun `requestUserLogin should set loginResult Data well`() {
        // Setup
        val emptyRequest = UserLoginRequest("", "")
        val emptyResponse = UserLoginResponse("testToken")
        `when`(mockUserRepository.loginUser(emptyRequest))
            .thenReturn(ExecutionResult(ResultType.Success, emptyResponse, ""))

        // Do
        userViewModel.requestUserLogin(emptyRequest)

        // Check
        val response = userViewModel.loginUser  .getOrAwaitValue()
        Assert.assertEquals(ResultType.Success, response.resultType)
        Assert.assertEquals(emptyResponse.userToken, response.value?.userToken)
    }
}