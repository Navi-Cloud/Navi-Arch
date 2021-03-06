package com.kangdroid.navi_arch.server

import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.FileType
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getFields
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.*
import java.io.File

class ServerManagementTest {

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

    // Server Management Object
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

    @Before
    @After
    fun init() {
        println("Clearing Server Data!")
        serverSetup.clearData()
    }

    // Init Test
    @Test
    fun is_initServerCommunication_works_well() {
        assertThat(serverManagement.initWholeServerClient())
            .isEqualTo(true)
    }

    // Check whether register works well or not.
    @Test
    fun is_register_works_well() {
        serverManagement.register(mockUserRegisterRequest).also {
            assertThat(it.registeredId).isEqualTo(mockUserRegisterRequest.userId)
            assertThat(it.registeredEmail).isEqualTo(mockUserRegisterRequest.userEmail)
        }
    }

    @Test
    fun is_login_works_well() {
        serverManagement.register(mockUserRegisterRequest)

        serverManagement.loginUser(
            userLoginRequest = LoginRequest(
                userId = mockUserRegisterRequest.userId,
                userPassword = mockUserRegisterRequest.userPassword
            )
        ).also {
            assertThat(it.userToken).isNotEqualTo("")
            assertThat(serverManagement.userToken).isEqualTo(it.userToken)
        }

    }

    // Root Token Test
    @Test
    fun is_getRootToken_works_well() {
        registerAndLogin()
        assertThat(serverManagement.getRootToken()).isNotEqualTo("")
    }

    // Get Inside Files
    @Test
    fun is_getInsideFiles_works_well() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken
        val result: List<FileData> = serverManagement.getInsideFiles(rootToken)
        assertThat(result.size).isEqualTo(0)
    }

    private fun uploadTest() {
        val rootToken: String = serverManagement.getRootToken().rootToken
        val mockFileContents: String = "Hello, World!"
        val mockResults: String = "20"

        // Tmp File
        val file: File = File(System.getProperty("java.io.tmpdir"), "test.txt").apply {
            writeText(mockFileContents)
        }

        val requestBody : RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),file)
        val uploadFile : MultipartBody.Part = MultipartBody.Part.createFormData("uploadFile","test.txt",requestBody)
        val param : HashMap<String,Any> = HashMap()
        with(param){
            put("uploadPath", rootToken)
        }

        runCatching {
            serverManagement.upload(param, uploadFile)
        }.onFailure {
            println(it.stackTraceToString())
            fail("Something went wrong. This should be succeed.")
        }.onSuccess {
            assertThat(it).contains(rootToken)
        }

        // Cleanup
        file.delete()
    }

    private fun downloadTest() {
        // Download part
        val rootToken: String = serverManagement.getRootToken().rootToken
        val fileList: List<FileData> = serverManagement.getInsideFiles(rootToken).also {
            assertThat(it.size).isEqualTo(1)
        }

        runCatching {
            serverManagement.download(fileList[0].token, fileList[0].prevToken)
        }.onFailure {
            fail("This test should passed since we mocked our server to be succeed.")
        }.onSuccess {
            assertThat(it.fileName).isEqualTo("test.txt")
        }
    }

    @Test
    fun is_upload_download_works_well() {
        registerAndLogin()
        uploadTest()
        downloadTest()
    }

    @Test
    fun is_createFolder_works_well() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Perform
        val folderName: String = "TeST"
        serverManagement.createFolder(
            CreateFolderRequestDTO(
                parentFolderToken = rootToken,
                newFolderName = folderName
            )
        )

        // Assert
        serverManagement.getInsideFiles(rootToken).also {
            assertThat(it.size).isEqualTo(1)
            assertThat(it[0].prevToken).isEqualTo(rootToken)
            assertThat(it[0].fileName).isEqualTo(folderName)
            assertThat(it[0].fileType).isEqualTo(FileType.Folder.toString())
        }
    }

    @Test
    fun is_removeFile_works_well() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Make Folder
        val folderName: String = "TeST"
        serverManagement.createFolder(
            CreateFolderRequestDTO(
                parentFolderToken = rootToken,
                newFolderName = folderName
            )
        )
        val requestFolderData: FileData = serverManagement.getInsideFiles(rootToken)[0]

        // Perform
        serverManagement.removeFile(rootToken, requestFolderData.token)

        // Assert
        serverManagement.getInsideFiles(rootToken).also {
            assertThat(it.size).isEqualTo(0)
        }
    }

    @Test
    fun is_searchFile_works_well() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Make Folder
        val folderName: String = "TeST"
        val partialFolderName: String = "eST"
        serverManagement.createFolder(
            CreateFolderRequestDTO(parentFolderToken = rootToken, newFolderName = folderName))
        serverManagement.createFolder(
            CreateFolderRequestDTO(parentFolderToken = rootToken, newFolderName = partialFolderName))

        // Perform & Assert
        serverManagement.searchFile(partialFolderName).also {
            assertThat(it.size).isEqualTo(2)
        }
    }

    @Test
    fun is_findFolderFromToken_works_well() {
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Make Folder
        val folderName: String = "TeST"
        serverManagement.createFolder(
            CreateFolderRequestDTO(parentFolderToken = rootToken, newFolderName = folderName))
        val requestFolderData: FileData = serverManagement.getInsideFiles(rootToken)[0]

        // Perform & Assert
        serverManagement.findFolderFromToken(requestFolderData.token).also {
            assertThat(it.fileName).isEqualTo(requestFolderData.fileName)
            assertThat(it.token).isEqualTo(requestFolderData.token)
        }
    }

    @Test
    fun is_initWholeServerClient_works_well() {
        val serverManagementNotConnected: ServerManagement = ServerManagement(null)

        getFields<ServerManagement, Boolean>("isServerEnabled", serverManagementNotConnected).also {
            assertThat(it).isEqualTo(false)
        }
    }
}