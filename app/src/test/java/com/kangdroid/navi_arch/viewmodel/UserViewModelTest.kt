package com.kangdroid.navi_arch.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getOrAwaitValue
import okhttp3.HttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.*

class UserViewModelTest{

    companion object {
        @JvmStatic
        val serverSetup: ServerSetup = ServerSetup(
            if (System.getProperty("os.name").contains("Windows")) {
                WindowsServerSetup()
            } else {
                LinuxServerSetup()
            }
        )

        @JvmStatic
        @BeforeClass
        fun setupServer() {
            println("Setting up server..")
            serverSetup.setupServer()
            println("Setting up server finished!")
        }

        @JvmStatic
        @AfterClass
        fun clearServer() {
            println("Clearing Server..")
            serverSetup.killServer(false)
            println("Clearing Server finished!")
        }
    }

    //Target
    private lateinit var userViewModel: UserViewModel

    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val serverManagement: ServerManagement = ServerManagement.getServerManagement(
        HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(8080)
            .build()
    )

    // Mock Register Request
    private val mockUserRegisterRequest: RegisterRequest = RegisterRequest(
        userId = "kangdroid",
        userPassword = "test",
        userEmail = "a@b.com",
        userName = "KangDroid"
    )

    private val mockUserRegisterRequestNotCorrect: RegisterRequest = RegisterRequest(
        userId = "wronguser",
        userPassword = "test",
        userEmail = "Ttest",
        userName = "KangDroid"
    )

    @Before
    fun init() {
        serverSetup.clearData()
        userViewModel = UserViewModel()
        ViewModelTestHelper.setFields("serverManagement", userViewModel, serverManagement)
    }

    @After
    fun destroy() {
        serverSetup.clearData()
    }

    @Test
    fun is_requestRegisterPage_works_well(){
        userViewModel.requestRegisterPage()

        userViewModel.pageRequest.getOrAwaitValue().also {
            assertThat(it).isEqualTo(PageRequest.REQUEST_REGISTER)
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
    fun is_clearErrorData_works_well(){
        userViewModel.clearErrorData()

        userViewModel.loginErrorData.getOrAwaitValue().also {
            assertThat(it).isEqualTo(null)
        }
    }

    @Test
    fun is_login_works_well(){
        serverManagement.register(mockUserRegisterRequest)
        userViewModel.login(mockUserRegisterRequest.userId, mockUserRegisterRequest.userPassword)

//        assertThat(userViewModel.loginresponse).isNotEqualTo(null)
        userViewModel.pageRequest.getOrAwaitValue().also {
            assertThat(it).isEqualTo(PageRequest.REQUEST_MAIN)
        }
    }

    @Test
    fun is_login_works_wrong(){
        userViewModel.login(mockUserRegisterRequestNotCorrect.userId, mockUserRegisterRequestNotCorrect.userPassword)

        assertThat(userViewModel.loginErrorData).isNotEqualTo(null)
    }

    @Test
    fun is_register_works_well(){
        userViewModel.register(
            mockUserRegisterRequest.userId,
            mockUserRegisterRequest.userName,
            mockUserRegisterRequest.userEmail,
            mockUserRegisterRequest.userPassword
        )

        userViewModel.pageRequest.getOrAwaitValue().also {
            assertThat(it).isEqualTo(PageRequest.REQUEST_LOGIN)
        }
    }
}