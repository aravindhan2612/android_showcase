package com.ab.couterexamplewithndk.domain.usecase

import com.ab.couterexamplewithndk.domain.model.CounterModel
import com.ab.couterexamplewithndk.domain.repository.CounterRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCountHistoryUseCase @Inject constructor(private val repository: CounterRepository){
    val history: Flow<List<CounterModel>> = repository.countHistories

    suspend fun clearHistory() {
        repository.resetCount()
    }
}