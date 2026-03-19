package com.ab.couterexamplewithndk.domain.repository

import com.ab.couterexamplewithndk.domain.model.CounterModel
import com.example.counterapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface CounterRepository {
    suspend fun incrementCount(): Resource<Int>
    val countHistories: Flow<List<CounterModel>>
    suspend fun resetCount()
}