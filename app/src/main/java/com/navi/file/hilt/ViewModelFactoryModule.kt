package com.navi.file.hilt

import com.navi.file.helper.ViewModelFactory
import com.navi.file.repository.server.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ViewModelFactoryModule {
    @Provides
    @Singleton
    fun createViewModelFactory(userRepository: UserRepository): ViewModelFactory = ViewModelFactory(userRepository)
}