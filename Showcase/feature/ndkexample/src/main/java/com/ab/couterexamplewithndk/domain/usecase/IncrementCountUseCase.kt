package com.ab.couterexamplewithndk.domain.usecase

import com.ab.couterexamplewithndk.domain.repository.CounterRepository
import com.example.counterapp.utils.Resource
import jakarta.inject.Inject

class IncrementCountUseCase @Inject constructor(private val repository: CounterRepository) {

    suspend operator fun invoke(): Resource<Int> = repository.incrementCount()
}