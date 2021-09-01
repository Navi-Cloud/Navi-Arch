package com.navi.file.repository.server.factory

import okhttp3.HttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NaviRetrofitFactory {
    // HttpUrl
    private var httpUrl: HttpUrl? = null

    // Retrofit Factory
    private var innerBase: Retrofit? = null

    // Get Base Retrofit from innerBase, only if object is not null.
    val baseRetrofit: Retrofit get() {
        return innerBase ?: throw IllegalStateException("Retrofit is NOT set!")
    }

    /**
     * Create Retrofit object.
     *
     * @param inputUrl Input base url for connecting server.
     */
    fun createRetrofit(inputUrl: HttpUrl) {
        if (httpUrl != inputUrl) {
            innerBase = null
            httpUrl = inputUrl
        }
        innerBase = innerBase ?: Retrofit.Builder()
            .baseUrl(httpUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}