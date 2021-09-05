package com.navi.file.repository.server.folder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.navi.file.model.ErrorResponseModel
import com.navi.file.model.FileMetadata
import com.navi.file.model.intercommunication.ResultType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class FolderRepositoryTest {
    private lateinit var testServer: MockWebServer
    private lateinit var folderRepository: FolderRepository
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val mockError: ErrorResponseModel = ErrorResponseModel(
        traceId = "",
        message = ""
    )

    @Before
    fun setupServer() {
        testServer = MockWebServer()

        // Setup User Server Repository
        NaviRetrofitFactory.createRetrofit(testServer.url(""))
        folderRepository = FolderRepository()
    }

    @Test
    fun `exploreFolder should return its list of file metadata if succeeds`() {
        // Let
        testServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(
                    objectMapper.writeValueAsString(
                        listOf(
                            FileMetadata("", "", "", false),
                            FileMetadata("", "", "", false),
                            FileMetadata("", "", "", false),
                            FileMetadata("", "", "", false),
                        )
                    )
                )
        )

        // Do
        val result = folderRepository.exploreFolder("targetFolderTest")

        // Assert.
        Assert.assertEquals(ResultType.Success, result.resultType)
        Assert.assertNotNull(result.value)
        Assert.assertEquals(4, result.value!!.size)
    }

    @Test
    fun `ExploreFolder should return unauthorized result if user is not authorized`() {
        // Let
        testServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody(objectMapper.writeValueAsString(mockError))
        )

        // Do
        val result = folderRepository.exploreFolder("")

        // Assert
        Assert.assertEquals(ResultType.Unauthorized, result.resultType)
    }
}