package com.ab.couterexamplewithndk.data.repository

import com.ab.couterexamplewithndk.domain.model.CounterModel
import com.ab.couterexamplewithndk.domain.repository.CounterRepository
import com.example.counterapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class CounterRepositoryImpl : CounterRepository {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    private external fun nativeIncrementCount(): Int
    private external fun nativeResetCount()

    private val _countHistories = MutableStateFlow<List<CounterModel>>(emptyList())


    override suspend fun incrementCount(): Resource<Int> = withContext(Dispatchers.IO) {
        try {
            val count = nativeIncrementCount()
            _countHistories.update { countHistory ->
                countHistory + CounterModel(
                    count = count, timeStamp = System.currentTimeMillis()
                )
            }
            return@withContext (Resource.Success(count))
        } catch (e: Exception) {
            return@withContext (Resource.Error(message = e.message))
        }
    }

    override val countHistories: Flow<List<CounterModel>>
        get() = _countHistories


    override suspend fun resetCount() = withContext(Dispatchers.IO) {
        nativeResetCount()
        _countHistories.value = emptyList()
    }
}