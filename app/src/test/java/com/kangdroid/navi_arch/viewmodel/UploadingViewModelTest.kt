package com.kangdroid.navi_arch.viewmodel

import android.app.Application
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito

class UploadingViewModelTest {

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

    private lateinit var uploadingViewModel : UploadingViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val serverManagement: ServerManagement = ServerManagement.getServerManagement(
        HttpUrl.Builder()
            .scheme("http")
            .host("172.30.1.37")
            .port(8080)
            .build()
    )


    @Before
    fun init() {
        serverSetup.clearData()
        //val mockApplication = Mockito.mock(Application::class.java)
        uploadingViewModel = UploadingViewModel(Application())
        ViewModelTestHelper.setFields("serverManagement", uploadingViewModel, serverManagement)
    }

    @After
    fun destroy() {
        serverSetup.clearData()
    }

    private val mockUri : Uri = Uri.parse("content://com.android.providers.media.documents/document/image%3A32")

    private lateinit var mockFile : MultipartBody.Part

    private lateinit var requestBody : RequestBody

    @Test
    fun is_createFileUri_working_well(){

        uploadingViewModel.createFileUri(mockUri)

        assertThat(uploadingViewModel.fileContentArray).isEqualTo("[B@fca0319")

        //getFileName
        assertThat(uploadingViewModel.fileName).isEqualTo("elmo3 - 복사본.jpeg")

        requestBody = RequestBody.create(
            contentType = "multipart/form-data".toMediaTypeOrNull(),
            content = uploadingViewModel.fileContentArray
        )

    }

    @Test
    fun is_upload_working_well(){

        uploadingViewModel.createFileUri(mockUri)
        uploadingViewModel.upload("Lw==") { }

        mockFile = MultipartBody.Part.createFormData("uploadFile","a.txt",requestBody)

        assertThat(uploadingViewModel.uploadFile).isEqualTo(mockFile)
    }

}