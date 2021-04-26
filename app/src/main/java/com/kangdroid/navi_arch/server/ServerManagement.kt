package com.kangdroid.navi_arch.server

import android.os.Environment
import android.util.Log
import com.kangdroid.navi_arch.data.FileData
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerManagement @Inject constructor() {

    private val logTag: String = this::class.java.simpleName
    private var retroFit: Retrofit? = null
    private var api: APIInterface? = null

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
            Log.e(logTag, "FATAL - SERVER INIT FAILED!!")
            Log.e(logTag, e.stackTraceToString())
            null
        }

        api = retroFit?.create(APIInterface::class.java) ?: run {
            Log.e(
                logTag,
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
            Log.e(logTag, "Error when getting root token from server.")
            Log.e(logTag, e.stackTraceToString())
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
            Log.e(logTag, "Error when getting directory list from server.")
            Log.e(logTag, e.stackTraceToString())
            null
        }
        return response?.body() ?: listOf()
    }

    fun upload(Param : HashMap<String,Any>, file: MultipartBody.Part) : String {
        val uploading: Call<ResponseBody>? = api?.upload(Param, file)
        val response: Response<ResponseBody>? = try{
            uploading?.execute()
        }catch (e:Exception){
            Log.e(logTag, "Error when uploading File.")
            Log.e(logTag, e.stackTraceToString())
            null
        }

        if (response?.isSuccessful == false) {
            Log.e(logTag, "Upload did not successful")
            Log.e(logTag, "Message: ${response.errorBody()?.string()}")
        }

        return response?.body()?.string() ?: ""
    }

    fun download(token: String) {
        val downloading : Call<ResponseBody> ?= api?.download(token)
        val response: Response<ResponseBody>?= runCatching {
            downloading?.execute()
        }.getOrElse {
            Log.e(logTag, "Error when downloading File.")
            Log.e(logTag, it.stackTraceToString())
            null
        }

        if (response != null) {
            // Get Content Name
            val header : String = response.headers().get("Content-Disposition").apply {
                // Since raw header is encoded with URL Scheme, decode it.
                URLDecoder.decode(this,"UTF-8")
            } ?: throw IllegalArgumentException("Content-Disposition is NEEDED somehow, but its missing!")

            // Get file Name from header
            val fileName : String = header.replace("attachment; filename=\"", "").let {
                it.substring(it.lastIndexOf("/")+1,it.length-1)
            }
            Log.e(logTag, "fileName : $fileName")
            Log.e(logTag, "Content : ${response.body().toString()}")

            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                val pa : String = Environment.getExternalStorageDirectory().toString() + "/Download"
                val file = File(pa, fileName)
                try{

                    val fileReader = ByteArray(4096)
                    val fileSize: Long ?= response.body()?.contentLength()
                    var fileSizeDownloaded: Long = 0

                    val inputStream : InputStream? = response.body()?.byteStream()
                    val outputStream = FileOutputStream(file)

                    while (true) {
                        val read: Int ?= inputStream?.read(fileReader)
                        if (read == -1) {
                            break
                        }
                        if (read != null) {
                            outputStream.write(fileReader, 0, read)
                            Log.e(logTag, "OutputStream : $outputStream")
                            fileSizeDownloaded += read.toLong()
                        }
                        Log.e("DOWNLOAD", "file download: $fileSizeDownloaded of $fileSize")
                    }
                    outputStream.flush()
                }catch (e:Exception){
                    Log.e("External Storage", "External Storage is not ready.")
                    Log.e("External Storage", e.stackTraceToString())
                }
            }
        }
    }
}