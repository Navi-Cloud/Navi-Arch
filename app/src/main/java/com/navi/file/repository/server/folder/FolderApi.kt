package com.navi.file.repository.server.folder

import com.navi.file.model.FileMetadata
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FolderApi {
    @GET("/api/folder")
    fun exploreFolder(@Query("targetFolder") targetFolder: String): Call<List<FileMetadata>>
}