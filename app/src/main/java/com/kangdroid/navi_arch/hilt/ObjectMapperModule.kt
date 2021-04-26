package com.kangdroid.navi_arch.hilt

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ObjectMapperModule {
    @Provides
    fun provideObjectMapper(): ObjectMapper {
        Log.d(this::class.java.simpleName, "Creating Kotlin Jackson Mapper!")
        return jacksonObjectMapper()
    }
}