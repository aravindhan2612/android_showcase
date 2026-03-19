package com.ab.couterexamplewithndk.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ab.couterexamplewithndk.data.repository.CounterRepositoryImpl
import com.example.counterapp.utils.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CounterRepositoryImplTest {

    private lateinit var repository: CounterRepositoryImpl

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun testIncrementCount() = runTest {
        coEvery { repository.incrementCount() } returns Resource.Success(1)
        val result = repository.incrementCount()
        assert(result is Resource.Success)
    }
}