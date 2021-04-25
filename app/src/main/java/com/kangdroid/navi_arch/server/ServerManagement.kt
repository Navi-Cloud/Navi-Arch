package com.kangdroid.navi_arch.server

import android.util.Log
import com.kangdroid.navi_arch.data.FileData
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServerManagement {

    private val TAG_SERVER_MANAGEMENT = "ServerManagement"
    private var retroFit: Retrofit? = null
    private var api: APIInterface? = null
    // Variable/Value Initiation Ends

    /**
     * initServerCommunication: Initiate basic API/Retrofit
     * Returns true if both retroFit/api is NOT-NULL,
     * false when either of retrofit/api is null
     */
    fun initServerCommunication(
        serverAddress: String = "192.168.0.46",
        serverPort: String = "8080"
    ): Boolean {
        retroFit = try {
            Retrofit.Builder()
                .baseUrl("http://$serverAddress:$serverPort")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        } catch (e: IllegalArgumentException) {
            // Log priority: Error[WTF is not allowed since it might terminate APP]
            Log.e(TAG_SERVER_MANAGEMENT, "FATAL - SERVER INIT FAILED!!")
            Log.e(TAG_SERVER_MANAGEMENT, e.stackTraceToString())
            null
        }

        api = retroFit?.create(APIInterface::class.java) ?: run {
            Log.e(
                TAG_SERVER_MANAGEMENT,
                "Server is NOT initiated, therefore api will not be implemented."
            )
            null
        }

        return (retroFit != null && api != null)
    }

    fun getRootToken(): String {
        val tokenFunction: Call<ResponseBody>? = api?.getRootToken()
        val response: Response<ResponseBody>? = try {
            tokenFunction?.execute()
        } catch (e: Exception) {
            Log.e(TAG_SERVER_MANAGEMENT, "Error when getting root token from server.")
            Log.e(TAG_SERVER_MANAGEMENT, e.stackTraceToString())
            null
        }
        return response?.body()?.string() ?: ""
    }

    /**
     * getInsideFiles: Get list of files/directories based on requested token
     * Param: The target token to request
     * Returns: List of FileResponseDTO[The Response] - could be empty.
     * Returns: NULL when error occurred.
     */
    fun getInsideFiles(requestToken: String): List<FileData> {
        val insiderFunction: Call<List<FileData>>? = api?.getInsideFiles(requestToken)
        val response: Response<List<FileData>>? = try {
            insiderFunction?.execute()
        } catch (e: Exception) {
            Log.e(TAG_SERVER_MANAGEMENT, "Error when getting directory list from server.")
            Log.e(TAG_SERVER_MANAGEMENT, e.stackTraceToString())
            null
        }
        return response?.body() ?: listOf()
    }

    fun upload(Param : HashMap<String,Any>, file: MultipartBody.Part) : String {
        val uploading: Call<ResponseBody>? = api?.upload(Param, file)
        val response: Response<ResponseBody>? = try{
            uploading?.execute()
        }catch (e:Exception){
            Log.e(TAG_SERVER_MANAGEMENT, "Error when uploading File.")
            Log.e(TAG_SERVER_MANAGEMENT, e.stackTraceToString())
            null
        }
        Log.i("upload", "SUCEEEEEDDDDD")
        return response?.body()?.string() ?: ""
    }
}