package com.pilotprep.app.repository

import com.pilotprep.app.data.repository.QuizRepository
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.PerformanceMonitor
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizRepositoryTest {

    @Test
    fun getQuestionsForPractice_withInvalidLimit_returnsFailure() = runTest(StandardTestDispatcher()) {
        val performanceMonitor = mockk<PerformanceMonitor>()
        val errorHandler = mockk<ErrorHandler>(relaxed = true)

        // Exécuter simplement le bloc fourni pour le type attendu par getQuestionsForPractice
        coEvery {
            performanceMonitor.measureOperation<Result<List<com.pilotprep.app.data.model.Question>>>(any(), any())
        } coAnswers {
            val op = secondArg<suspend () -> Result<List<com.pilotprep.app.data.model.Question>>>()
            op.invoke()
        }

        val repository = QuizRepository(performanceMonitor, errorHandler)

        val result = repository.getQuestionsForPractice("CPL", "Navigation", limit = 0)

        assertTrue(result.isFailure)
    }
}

