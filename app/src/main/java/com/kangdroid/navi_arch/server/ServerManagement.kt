package com.kangdroid.navi_arch.server

import android.util.Log
import com.kangdroid.navi_arch.data.FileData
import com.kangdroid.navi_arch.data.dto.request.LoginRequest
import com.kangdroid.navi_arch.data.dto.request.RegisterRequest
import com.kangdroid.navi_arch.data.dto.response.DownloadResponse
import com.kangdroid.navi_arch.data.dto.response.LoginResponse
import com.kangdroid.navi_arch.data.dto.response.RootTokenResponseDto
import com.kangdroid.navi_arch.data.dto.response.UserRegisterResponse
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder

class ServerManagement(
    private val httpUrl: HttpUrl,
    private val serverManagementHelper: ServerManagementHelper
): ServerInterface {

    private val logTag: String = this::class.java.simpleName
    private lateinit var retroFit: Retrofit
    private lateinit var api: APIInterface

    // Whether server connection is successful or not
    private var isServerEnabled: Boolean = false

    // User Token
    var userToken: String? = null

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
        // Set headers
        // for now, set userToken
        val headers : HashMap<String, Any> = HashMap()
        headers["X-AUTH-TOKEN"] = userToken!!

        val tokenFunction: Call<RootTokenResponseDto> = api.getRootToken(headers)

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
        // Set headers
        // for now, set userToken
        val headers : HashMap<String, Any> = HashMap()
        headers["X-AUTH-TOKEN"] = userToken!!

        val insiderFunction: Call<List<FileData>> = api.getInsideFiles(headers, requestToken)
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
        // Set headers
        // for now, set userToken
        val headers : HashMap<String, Any> = HashMap()
        headers["X-AUTH-TOKEN"] = userToken!!

        val uploading: Call<ResponseBody> = api.upload(headers, Param, file)
        val response: Response<ResponseBody> =
            serverManagementHelper.exchangeDataWithServer(uploading)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        val responseBody: ResponseBody = response.body()!!

        return responseBody.string()
    }

    override fun download(token: String): DownloadResponse {
        // Set headers
        // for now, set userToken
        val headers : HashMap<String, Any> = HashMap()
        headers["X-AUTH-TOKEN"] = userToken ?: ""

        val downloadingApi: Call<ResponseBody> = api.download(headers, token)
        val response: Response<ResponseBody> =
            serverManagementHelper.exchangeDataWithServer(downloadingApi)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
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
        Log.d(logTag, "Content : ${response.body()?.string()}")

        return DownloadResponse(fileName, response.body()!!)
    }

    override fun loginUser(userLoginRequest: LoginRequest): LoginResponse {
        val loginUserRequest: Call<ResponseBody> = api.loginUser(userLoginRequest)
        val response: Response<ResponseBody> =
            serverManagementHelper.exchangeDataWithServer(loginUserRequest)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        // For get userToken, parsing
        val responseArray = response.body()!!.string()
            .replace(Regex("[\"{} ]"), "")
            .split(Regex("[:,]"))
        val userTokenResponse: String = responseArray[responseArray.indexOf("userToken")+1]

        // Save userToken
        userToken = userTokenResponse
        Log.d(logTag, "userToken---> $userToken")

        return LoginResponse(userToken = userTokenResponse)
    }

    override fun register(userRegisterRequest: RegisterRequest): UserRegisterResponse {
        val registerUserRequest : Call<ResponseBody> = api.register(userRegisterRequest)

        val response: Response<ResponseBody> =
            serverManagementHelper.exchangeDataWithServer(registerUserRequest)

        if (!response.isSuccessful) {
            Log.e(logTag, "${response.code()}")
            serverManagementHelper.handleDataError(response)
        }

        // For get userToken, parsing
        val responseArray = response.body()!!.string()
            .replace(Regex("[\"{} ]"), "")
            .split(Regex("[:,]"))
        val userId: String = responseArray[responseArray.indexOf("registeredId")+1]
        val userEmail: String = responseArray[responseArray.indexOf("registeredEmail")+1]

        Log.d(logTag, "registeredId---> $userId")
        Log.d(logTag, "registeredEmail---> $userEmail")

        return UserRegisterResponse(
            registeredId = userId,
            registeredEmail = userEmail
        )
    }
}