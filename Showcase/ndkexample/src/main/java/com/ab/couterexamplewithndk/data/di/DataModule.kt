package com.ab.couterexamplewithndk.data.di

import com.ab.couterexamplewithndk.data.repository.CounterRepositoryImpl
import com.ab.couterexamplewithndk.domain.repository.CounterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideCounterRepository(): CounterRepository {
        return CounterRepositoryImpl()
    }
}