package com.kangdroid.navi_arch.server

import android.util.Log
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.request.CreateFolderRequestDTO
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.DownloadResponse
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import com.kangdroid.navi_arch.data.dto.response.RegisterResponse
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder
import javax.inject.Singleton

class ServerManagement(
    private val httpUrl: HttpUrl
): ServerInterface {

    companion object {
        private var serverManagement: ServerManagement? = null
        private val logTag: String = this::class.java.simpleName

        fun getServerManagement(): ServerManagement {
            if (serverManagement == null) {
                Log.d(logTag, "Creating Server Management!")
                val defaultHttpUrl: HttpUrl = HttpUrl.Builder()
                    .scheme("http")
                    .host("172.30.1.26")
                    .port(8080)
                    .build()
                serverManagement = ServerManagement(defaultHttpUrl)
            }
            return serverManagement!!
        }
    }
    private val serverManagementHelper: ServerManagementHelper = ServerManagementHelper

    private lateinit var retroFit: Retrofit
    private lateinit var api: APIInterface

    // Whether server connection is successful or not
    private var isServerEnabled: Boolean = false

    // User Token
    var userToken: String? = null

    // Headers
    private fun getHeaders(): HashMap<String, Any?>{
        // Set headers
        // for now, set userToken
        val headers : HashMap<String, Any?> = HashMap()
        headers["X-AUTH-TOKEN"] = userToken
        return headers
    }

    init {
        isServerEnabled = initWholeServerClient()
    }

    override fun initWholeServerClient(): Boolean {
        var returnServerEnabled: Boolean = false
        runCatching {
            retroFit = initRetroFit()
            api = initApiInterface()
        }.onSuccess {
            returnServerEnabled = true
        }.onFailure {
            Log.e(logTag, "Error occurred when connecting to server: ${it.message}")
            Log.e(logTag, "Error Message: ${it.stackTraceToString()}")
            returnServerEnabled = false
        }

        return returnServerEnabled
    }

    // Might throw exceptions
    private fun initRetroFit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(httpUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun initApiInterface(): APIInterface {
        return retroFit.create(APIInterface::class.java)
    }


    override fun getRootToken(): RootTokenResponseDto {
        val tokenFunction: Call<RootTokenResponseDto> = api.getRootToken(
            headerMap = getHeaders()
        )

        // Get response, and throw if exception occurred.
        val response: Response<RootTokenResponseDto> =
            serverManagementHelper.exchangeDataWithServer(tokenFunction)

        // Check for input response
        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        return response.body()
            ?: throw NoSuchFieldException("Response was OK, but wrong response body received.")
    }

    /**
     * getInsideFiles: Get list of files/directories based on requested token
     * Param: The target token to request
     * Returns: List of FileResponseDTO[The Response] - could be empty.
     * Returns: NULL when error occurred.
     */
    override fun getInsideFiles(requestToken: String): List<FileData> {
        val insiderFunction: Call<List<FileData>> = api.getInsideFiles(
            headerMap = getHeaders(),
            token = requestToken)

        val response: Response<List<FileData>> =
            serverManagementHelper.exchangeDataWithServer(insiderFunction)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        return response.body()
            ?: throw NoSuchFieldException("Response was OK, but wrong response body received.")
    }

    override fun upload(Param: HashMap<String, Any>, file: MultipartBody.Part): String {
        val uploading: Call<ResponseBody> = api.upload(
            headerMap = getHeaders(),
            par = Param,
            files = file)

        val response: Response<ResponseBody> =
            serverManagementHelper.exchangeDataWithServer(uploading)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        val responseBody: ResponseBody = response.body()!!

        return responseBody.string()
    }

    override fun download(token: String, prevToken: String): DownloadResponse {
        val downloadingApi: Call<ResponseBody> = api.download(
            headerMap = getHeaders(),
            token = token,
            prevToken = prevToken
        )

        val response: Response<ResponseBody> =
            serverManagementHelper.exchangeDataWithServer(downloadingApi)

        if (!response.isSuccessful) {
            Log.e(logTag, "Token: $token , Prev: $prevToken")
            Log.e(logTag, "Error: ${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        // Get Content Name
        val header: String = response.headers()["Content-Disposition"].apply {
            // Since raw header is encoded with URL Scheme, decode it.
            URLDecoder.decode(this, "UTF-8")
        } ?: throw IllegalArgumentException("Content-Disposition is NEEDED somehow, but its missing!")

        // Get file Name from header
        val fileName: String = header.replace("attachment; filename=\"", "").let {
            it.substring(it.lastIndexOf("/") + 1, it.length - 1)
        }
        Log.d(logTag, "fileName : $fileName")

        return DownloadResponse(fileName, response.body()!!)
    }

    override fun loginUser(userLoginRequest: LoginRequest): LoginResponse {
        val loginUserRequest: Call<LoginResponse> = api.loginUser(userLoginRequest)
        val response: Response<LoginResponse> =
            serverManagementHelper.exchangeDataWithServer(loginUserRequest)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        userToken = response.body()!!.userToken
        Log.d(logTag, "userToken---> $userToken")

        return response.body()!!
    }

    override fun register(userRegisterRequest: RegisterRequest): RegisterResponse {
        val registerUserRequest : Call<RegisterResponse> = api.register(userRegisterRequest)

        val response: Response<RegisterResponse> =
            serverManagementHelper.exchangeDataWithServer(registerUserRequest)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        return response.body()!!
    }

    override fun createFolder(createFolderRequestDTO: CreateFolderRequestDTO) {
        val createFolderRequest: Call<ResponseBody> = api.createNewFolder(getHeaders(), createFolderRequestDTO)

        val response: Response<ResponseBody> =
            serverManagementHelper.exchangeDataWithServer(createFolderRequest)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }
    }

    override fun removeFile(prevToken: String, targetToken: String) {
        val removeRequest: Call<ResponseBody> = api.removeFile(getHeaders(), prevToken, targetToken)

        val response: Response<ResponseBody> =
            serverManagementHelper.exchangeDataWithServer(removeRequest)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }
    }
}