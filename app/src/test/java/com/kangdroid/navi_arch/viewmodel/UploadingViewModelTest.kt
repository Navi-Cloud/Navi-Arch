package com.kangdroid.navi_arch.viewmodel

import android.app.Application
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.setup.LinuxServerSetup
import com.kangdroid.navi_arch.setup.ServerSetup
import com.kangdroid.navi_arch.setup.WindowsServerSetup
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getFields
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper.getFunction
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowContentResolver
import java.io.ByteArrayInputStream
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
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

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Target Object
    private lateinit var uploadingViewModel : UploadingViewModel

    // For Robolectric
    private lateinit var application: Application
    private lateinit var contentResolver: ContentResolver
    private lateinit var shadowContentResolver: ShadowContentResolver

    // Global 'right' file name
    private val testFileName: String = "test_file_name"

    // Create "com.android.providers.media.documents" author provided URI
    val normalUri: Uri = Uri.parse("content://com.android.providers.media.documents/document/image%3A32")

    // Mock Server Management
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

    // Server Registration Function
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
    fun init() {
        // Setup Robolectric Required Data
        application = RuntimeEnvironment.getApplication()
        contentResolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
        shadowContentResolver = shadowOf(contentResolver)
        uploadingViewModel = UploadingViewModel(application)

        serverSetup.clearData()
        ViewModelTestHelper.setFields("serverManagement", uploadingViewModel, serverManagement)
    }

    @After
    fun destroy() {
        serverSetup.clearData()
    }

    /**
     * Setup function for mocking DocumentProvider.
     * This function will setup com.android.providers.media.document's Internal DB and its provider.
     */
    private fun setUpForFileOperation() {
        // Register File Name
        val cursor: MatrixCursor = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME)).apply {addRow(listOf(testFileName))}

        // Register Content Provider
        ShadowContentResolver.registerProviderInternal("com.android.providers.media.documents", object: ContentProvider() {
            override fun onCreate(): Boolean = false

            override fun query(
                uri: Uri,
                projection: Array<out String>?,
                selection: String?,
                selectionArgs: Array<out String>?,
                sortOrder: String?
            ): Cursor = cursor

            override fun getType(uri: Uri): String? = null

            override fun insert(uri: Uri, values: ContentValues?): Uri = normalUri

            override fun delete(
                uri: Uri,
                selection: String?,
                selectionArgs: Array<out String>?
            ): Int = -1

            override fun update(
                uri: Uri,
                values: ContentValues?,
                selection: String?,
                selectionArgs: Array<out String>?
            ): Int = -1

        })
    }

    @Test
    fun is_getFileName_works_well_from_document_provider() {
        // Register File Name
        setUpForFileOperation()

        val targetString: String = getFunction<UploadingViewModel>("getFileName")
            .call(uploadingViewModel, normalUri) as String

        assertThat(targetString).isEqualTo(testFileName)
    }

    @Test
    fun is_getFileName_works_well_from_other_provider() {
        // Create "com.android.providers.media.documents" author provided URI
        val mockUri: Uri = Uri.parse("content://other.provider.test/document/$testFileName")

        val targetString: String = getFunction<UploadingViewModel>("getFileName")
            .call(uploadingViewModel, mockUri) as String

        assertThat(targetString).isEqualTo(testFileName)
    }

    @Test
    fun is_getFileName_works_well_from_non_provider() {
        val mockUri: Uri = Uri.parse("/home/kangdroid/$testFileName")

        val targetString: String = getFunction<UploadingViewModel>("getFileName")
            .call(uploadingViewModel, mockUri) as String

        assertThat(targetString).isEqualTo(testFileName)
    }

    @Test
    fun is_createFileUri_works_well() {
        // Create "com.android.providers.media.documents" author provided URI
        val expectedString: String = "TestWhatever"

        // Register Input Stream
        shadowContentResolver.registerInputStream(normalUri, ByteArrayInputStream(expectedString.toByteArray()))

        // Setup for File operation
        setUpForFileOperation()

        // Do execute
        uploadingViewModel.createFileUri(normalUri)

        // Check
        getFields<UploadingViewModel, ByteArray>("fileContentArray", uploadingViewModel).also {
            assertThat(String(it)).isEqualTo(expectedString)
        }
        getFields<UploadingViewModel, String>("fileName", uploadingViewModel).also {
            assertThat(it).isEqualTo(testFileName)
        }
    }

    @Test
    fun is_upload_works_well() {
        // Server Setup
        registerAndLogin()
        val rootToken: String = serverManagement.getRootToken().rootToken

        // Create "com.android.providers.media.documents" author provided URI
        val expectedString: String = "TestWhatever"

        // Register Input Stream
        shadowContentResolver.registerInputStream(normalUri, ByteArrayInputStream(expectedString.toByteArray()))

        // Setup for File operation
        setUpForFileOperation()

        // Create file contents
        uploadingViewModel.createFileUri(normalUri)

        // Do execute
        runBlocking {
            uploadingViewModel.upload(rootToken) {}
            sleep(1500) // Wait for server confirms
            serverManagement.getInsideFiles(rootToken).also {
                assertThat(it.size).isEqualTo(1)
                assertThat(it[0].fileName).isEqualTo(testFileName)
            }
        }
    }
}