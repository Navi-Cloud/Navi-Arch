package com.kangdroid.navi_arch.hilt

import android.util.Log
import com.kangdroid.navi_arch.server.ServerManagement
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl

@Module
@InstallIn(SingletonComponent::class)
class ServerModule {

    @Provides
    fun provideServerManagement(): ServerManagement {
        Log.d(this::class.java.simpleName, "Creating Server Management!")
        val defaultHttpUrl: HttpUrl = HttpUrl.Builder()
            .scheme("http")
            .host("192.168.0.46")
            .port(8080)
            .build()
        return ServerManagement(defaultHttpUrl)
    }
}