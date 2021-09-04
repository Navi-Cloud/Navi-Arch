package com.navi.file.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.user.UserRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class RegisterViewModelTest: ViewModelHelper() {
    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private var mockUserRepository: UserRepository = Mockito.mock(UserRepository::class.java)
    private var registerViewModel: RegisterViewModel = RegisterViewModel(
        userRepository = mockUserRepository,
        dispatcherInfo = DispatcherInfo(
            uiDispatcher = Dispatchers.Unconfined,
            ioDispatcher = Dispatchers.Unconfined,
            backgroundDispatcher = Dispatchers.Unconfined
        )
    )

    @Test
    fun `Default constructor should create its instance well`() {
        val registerViewModel = RegisterViewModel(mockUserRepository)
        Assert.assertNotNull(registerViewModel)
    }

    @Test
    fun `requestUserRegister should set registerResult Data well`() {
        // Setup
        whenever(mockUserRepository.registerUser(any()))
            .thenReturn(ExecutionResult(ResultType.Success, null, ""))

        // Do
        registerViewModel.requestUserRegister("kangdroid@testwhatever.com", "", "asdfasdf!@#$")

        // Check
        val response = registerViewModel.registerResult.getOrAwaitValue()
        Assert.assertEquals(ResultType.Success, response.resultType)
    }

    @Test
    fun `requestUserRegister should return ModelValidateFailed when model is not valid`() {
        // Do
        registerViewModel.requestUserRegister("kangdroid@", "", "!")

        // Check
        val response = registerViewModel.registerResult.getOrAwaitValue()
        Assert.assertEquals(ResultType.ModelValidateFailed, response.resultType)
    }
}