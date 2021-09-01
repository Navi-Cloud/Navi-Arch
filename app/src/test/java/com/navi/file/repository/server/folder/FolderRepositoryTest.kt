package com.navi.file.repository.server.folder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.navi.file.model.ErrorResponseModel
import com.navi.file.model.FileMetadata
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.factory.NaviRetrofitFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection

class FolderRepositoryTest {
    private lateinit var testServer: MockWebServer
    private lateinit var folderRepository: FolderRepository
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val mockError: ErrorResponseModel = ErrorResponseModel(
        traceId = "",
        message = ""
    )

    @BeforeEach
    fun setupServer() {
        testServer = MockWebServer()

        // Setup User Server Repository
        NaviRetrofitFactory.createRetrofit(testServer.url(""))
        folderRepository = FolderRepository()
    }

    @Test
    @DisplayName("exploreFolder: exploreFolder should return its list of file metadata if succeeds.")
    fun is_exploreFolder_works_well() {
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
    @DisplayName("exploreFolder: ExploreFolder should return unauthorized result if user is not authorized.")
    fun is_exploreFolder_returns_unauthorized_when_no_toke() {
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