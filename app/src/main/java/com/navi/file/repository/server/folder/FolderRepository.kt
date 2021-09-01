package com.navi.file.repository.server.folder

import com.navi.file.model.FileMetadata
import com.navi.file.repository.server.factory.ExecutionResult
import com.navi.file.repository.server.factory.NaviRetrofitFactory
import com.navi.file.repository.server.factory.ResultType
import com.navi.file.repository.server.factory.ServerRepositoryBase
import retrofit2.Response
import retrofit2.create
import java.net.HttpURLConnection

class FolderRepository: ServerRepositoryBase() {
    // Create API Interface
    private val folderRepositoryApi: FolderApi = NaviRetrofitFactory.baseRetrofit.create()

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