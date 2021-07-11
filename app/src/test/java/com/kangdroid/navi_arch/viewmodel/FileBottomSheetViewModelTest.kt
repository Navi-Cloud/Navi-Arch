package com.kangdroid.navi_arch.viewmodel

import android.os.Environment
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.DownloadResponse
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getFunction
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowEnvironment
import java.io.File
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class FileBottomSheetViewModelTest {

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


    // Rule that every android-thread should launched in single thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fileBottomSheetViewModel: FileBottomSheetViewModel

    @Before
    fun init() {
        serverSetup.clearData()
        fileBottomSheetViewModel = FileBottomSheetViewModel()
        ViewModelTestHelper.setFields("serverManagement", fileBottomSheetViewModel, serverManagement)
    }

    @After
    fun destroy() {
        serverSetup.clearData()
    }

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
        userEmail = "Ttest",
        userName = "KangDroid"
    )

    private fun registerAndLogin() {
        serverManagement.register(mockUserRegisterRequest)
        serverManagement.loginUser(
            LoginRequest(
                userId = mockUserRegisterRequest.userId,
                userPassword = mockUserRegisterRequest.userPassword
            )
        )
    }

    private fun uploadTestFileToRoot() {
        val rootToken: String = serverManagement.getRootToken().rootToken
        val mockFileContents: String = "Hello, World!"

        // Tmp File
        val file: File = File(System.getProperty("java.io.tmpdir"), "test.txt").apply {
            writeText(mockFileContents)
        }

        val requestBody : RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),file)
        val uploadFile : MultipartBody.Part = MultipartBody.Part.createFormData("uploadFile","test.txt",requestBody)
        val param : HashMap<String,Any> = HashMap<String, Any>().apply {
            put("uploadPath", rootToken)
        }
        serverManagement.upload(param, uploadFile)

        // Cleanup
        file.delete()
    }

    @Test
    fun is_saveDownloadFile_works_well() {
        // Set Environment state to media_mounted
        val storagePath: File = File(System.getProperty("java.io.tmpdir"), "tmpRoot").apply {
            mkdir()
        }
        val downloadFile: File = File(storagePath, "Download").apply {mkdir()}
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED)
        ShadowEnvironment.setExternalStorageDirectory(storagePath.toPath())

        // Setup server
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Upload file
        uploadTestFileToRoot()

        // Get Uploaded file information
        val uploadResult: FileData = serverManagement.getInsideFiles(rootToken)[0]

        // Download it
        val downloadResult: DownloadResponse = serverManagement.download(uploadResult.token, uploadResult.prevToken)

        // Do Execute
        getFunction<FileBottomSheetViewModel>("saveDownloadedFile").call(
            fileBottomSheetViewModel, downloadResult
        )

        // Check
        assertThat(File(downloadFile, "test.txt").exists()).isEqualTo(true)

        // Cleanup
        storagePath.deleteRecursively()
    }

    @Test
    fun is_saveDownloadFile_throws_IllegalStateException_not_mounted() {
        // Setup server
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Upload file
        uploadTestFileToRoot()

        // Get Uploaded file information
        val uploadResult: FileData = serverManagement.getInsideFiles(rootToken)[0]

        // Download it
        val downloadResult: DownloadResponse = serverManagement.download(uploadResult.token, uploadResult.prevToken)

        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_REMOVED)
        runCatching {
            getFunction<FileBottomSheetViewModel>("saveDownloadedFile").call(
                fileBottomSheetViewModel, downloadResult
            )
        }.onFailure {
            assertThat(it.cause is IllegalStateException).isEqualTo(true)
        }.onSuccess {
            fail("Media Storage is removed but it succeed to save?")
        }
    }

    @Test
    fun is_removeFile_works_well_file() {
        // Setup server
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Upload file
        uploadTestFileToRoot()

        // Get Uploaded file information
        val uploadResult: FileData = serverManagement.getInsideFiles(rootToken)[0]

        // doExecute
        runBlocking {
            fileBottomSheetViewModel.removeFile(
                prevToken = uploadResult.prevToken,
                targetToken = uploadResult.token,
                onSuccess = {},
                onFailure = {}
            )
            sleep(1500)
        }

        // Check
        serverManagement.getInsideFiles(rootToken).also {
            assertThat(it.size).isEqualTo(0)
        }
    }

    @Test
    fun is_removeFile_works_well_folder() {
        // Setup server
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Upload file
        serverManagement.createFolder(
            CreateFolderRequestDTO(rootToken, "TestFolder")
        )

        // Get Uploaded file information
        val uploadResult: FileData = serverManagement.getInsideFiles(rootToken)[0]

        // doExecute
        runBlocking {
            fileBottomSheetViewModel.removeFile(
                prevToken = uploadResult.prevToken,
                targetToken = uploadResult.token,
                onSuccess = {},
                onFailure = {}
            )
            sleep(1500)
        }

        // Check
        serverManagement.getInsideFiles(rootToken).also {
            assertThat(it.size).isEqualTo(0)
        }
    }

    @Test
    fun is_downloadFile_works_well() {
        // Set Environment state to media_mounted
        val storagePath: File = File(System.getProperty("java.io.tmpdir"), "tmpRoot").apply {
            mkdir()
        }
        val downloadFile: File = File(storagePath, "Download").apply {mkdir()}
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED)
        ShadowEnvironment.setExternalStorageDirectory(storagePath.toPath())

        // Setup server
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Upload file
        uploadTestFileToRoot()

        // Get Uploaded file information
        val uploadResult: FileData = serverManagement.getInsideFiles(rootToken)[0]

        // Do Execute
        runBlocking {
            fileBottomSheetViewModel.downloadFile(uploadResult.token, uploadResult.prevToken)
            sleep(1500)
        }

        // Check
        assertThat(File(downloadFile, "test.txt").exists()).isEqualTo(true)

        // Cleanup
        storagePath.deleteRecursively()
    }

}