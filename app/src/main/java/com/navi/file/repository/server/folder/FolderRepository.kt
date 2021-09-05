package com.navi.file.repository.server.folder

import com.navi.file.model.FileMetadata
import com.navi.file.model.intercommunication.ExecutionResult
import com.navi.file.model.intercommunication.ResultType
import com.navi.file.repository.server.factory.ServerRepositoryBase
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import java.net.HttpURLConnection
import javax.inject.Inject

class FolderRepository @Inject constructor(
    baseRetrofit: Retrofit
): ServerRepositoryBase() {
    // Create API Interface
    private val folderRepositoryApi: FolderApi = baseRetrofit.create()

    /**
     * Explore Folder with given targetFolder.
     *
     * @param targetFolder Target Folder to explore!
     * @return Execution Result - List of File Metadata.
     */
    fun exploreFolder(targetFolder: String): ExecutionResult<List<FileMetadata>> {
        // Handled Case
        val handledCase: HashMap<Int, (Response<List<FileMetadata>>) -> ExecutionResult<List<FileMetadata>>> = hashMapOf(
            HttpURLConnection.HTTP_OK to {
                ExecutionResult(ResultType.Success, value = it.body(), message = "")
            },
            HttpURLConnection.HTTP_UNAUTHORIZED to {
                ExecutionResult(ResultType.Unauthorized, value = null, message = getErrorMessage(it))
            }
        )

        return folderRepositoryApi.exploreFolder(targetFolder).getExecutionResult(handledCase)
    }
}