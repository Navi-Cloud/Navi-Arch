package com.kangdroid.navi_arch.hilt

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.navi_arch.server.ServerInterface
import com.kangdroid.navi_arch.server.ServerManagement
import com.kangdroid.navi_arch.server.ServerManagementHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl

@Module(includes = [ObjectMapperModule::class])
@InstallIn(SingletonComponent::class)
class ServerModule {

    @Provides
    fun provideServerManagementHelper(objectMapper: ObjectMapper): ServerManagementHelper {
        return ServerManagementHelper(objectMapper)
    }

    @Provides
    fun provideServerManagement(serverManagementHelper: ServerManagementHelper): ServerInterface {
        Log.d(this::class.java.simpleName, "Creating Server Management!")
        val defaultHttpUrl: HttpUrl = HttpUrl.Builder()
            .scheme("http")
            .host("192.168.0.46")
            .port(8080)
            .build()
        return ServerManagement(defaultHttpUrl, serverManagementHelper)
    }
}