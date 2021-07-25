package com.kangdroid.navi_arch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RegisterResponse
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.*

class UserViewModelTest{
    //Target
    private lateinit var userViewModel: UserViewModel

    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Mock ServerManagement for UserViewModel
    private val mockServerManagement: ServerManagement = mockk()

    private val testCorrectUserId: String = "user!"
    private val testIncorrectUserId: String = "not user"

    @Before
    fun init() {
        userViewModel = UserViewModel()
        ViewModelTestHelper.setFields("serverManagement", userViewModel, mockServerManagement)
    }

    @Test
    fun is_requestRegisterPage_works_well(){
        userViewModel.requestRegisterPage()

        userViewModel.pageRequest.getOrAwaitValue().also {
            assertThat(it).isEqualTo(PageRequest.REQUEST_REGISTER)
        }
    }

    @Test
    fun is_requestMainPage_works_well() {
        ViewModelTestHelper.getFunction<UserViewModel>("requestMainPage")
            .call(userViewModel)

        userViewModel.pageRequest.getOrAwaitValue().also {
            assertThat(it).isEqualTo(PageRequest.REQUEST_MAIN)
        }
    }

    @Test
    fun is_requestLoginPage_works_well(){
        userViewModel.requestLoginPage()

        userViewModel.pageRequest.getOrAwaitValue().also {
            assertThat(it).isEqualTo(PageRequest.REQUEST_LOGIN)
        }
    }

    @Test
    fun is_loginError_works_well(){
        val exception : Throwable = Throwable()
        userViewModel.loginError(exception)

        userViewModel.loginErrorData.getOrAwaitValue().also {
            assertThat(it).isEqualTo(exception)
        }
    }

    @Test
    fun is_registerError_works_well(){
        val exception : Throwable = Throwable()
        userViewModel.registerError(exception)

        userViewModel.registerErrorData.getOrAwaitValue().also {
            assertThat(it).isEqualTo(exception)
        }
    }

    @Test
    fun is_clearLoginErrorData_works_well(){
        userViewModel.clearLoginErrorData()

        userViewModel.loginErrorData.getOrAwaitValue().also {
            assertThat(it).isEqualTo(null)
        }
    }

    @Test
    fun is_clearRegisterErrorData_works_well(){
        userViewModel.clearRegisterErrorData()

        userViewModel.registerErrorData.getOrAwaitValue().also {
            assertThat(it).isEqualTo(null)
        }
    }

    @Test
    fun is_login_works_well(){
        // mock server setting
        val testUserToken: String = "token"
        every {
            mockServerManagement.loginUser(match {
                it.userId == testCorrectUserId
            })
        } returns LoginResponse(testUserToken)

        // Perform
        userViewModel.login(
            userId = testCorrectUserId,
            userPassword = "pw"
        )

        // Assert
        userViewModel.pageRequest.getOrAwaitValue().also {
            assertThat(it).isEqualTo(PageRequest.REQUEST_MAIN)
        }
        assertThat(userViewModel.loginresponse!!.userToken).isEqualTo(testUserToken)
    }

    @Test
    fun is_login_works_when_error(){
        // mock server setting
        val exception : Throwable = Throwable()
        every {
            mockServerManagement.loginUser(match {
                it.userId == testIncorrectUserId
            })
        } throws exception

        // Perform
        userViewModel.login(
            userId = testIncorrectUserId,
            userPassword = "pw"
        )

        // Assert
        userViewModel.loginErrorData.getOrAwaitValue().also {
            assertThat(it).isEqualTo(exception)
        }
    }

    @Test
    fun is_register_works_well(){
        // mock server setting
        every {
            mockServerManagement.register(match {
                it.userId == testCorrectUserId
            })
        } returns RegisterResponse(testCorrectUserId, "test")

        // Perform
        userViewModel.register(
            userId = testCorrectUserId,
            userName = "name",
            userEmail = "email",
            userPassword = "pw"
        )

        // Assert
        userViewModel.pageRequest.getOrAwaitValue().also {
            assertThat(it).isEqualTo(PageRequest.REQUEST_LOGIN)
        }
    }

    @Test
    fun is_register_works_when_error() {
        // mock server setting
        val exception : Throwable = Throwable()
        every {
            mockServerManagement.register(match {
                it.userId == testIncorrectUserId
            })
        } throws exception

        // Perform
        userViewModel.register(
            userId = testIncorrectUserId,
            userName = "name",
            userEmail = "email",
            userPassword = "pw"
        )

        // Assert
        userViewModel.registerErrorData.getOrAwaitValue().also {
            assertThat(it).isEqualTo(exception)
        }
    }
}