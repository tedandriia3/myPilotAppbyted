package com.pilotprep.app.viewmodels

import com.pilotprep.app.data.model.Question
import com.pilotprep.app.data.repository.QuizRepository
import com.pilotprep.app.data.repository.UserStatsRepository
import com.pilotprep.app.services.UserStatsService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @Test
    fun startQuiz_andAnswerFlow_updatesScoreAndFinishes() = runTest(StandardTestDispatcher()) {
        val quizRepository = mockk<QuizRepository>()
        val userStatsRepository = mockk<UserStatsRepository>(relaxed = true)
        val userStatsService = mockk<UserStatsService>(relaxed = true)

        val questions = listOf(
            Question(id = "q1", license = "PPL", subject = "Met", questionText = "Q1?", options = listOf("A","B","C","D"), correctOptionIndex = 2),
            Question(id = "q2", license = "PPL", subject = "Met", questionText = "Q2?", options = listOf("A","B","C","D"), correctOptionIndex = 1)
        )
        coEvery { quizRepository.getQuestionsForPractice(any(), any(), any()) } returns Result.success(questions)

        val vm = QuizViewModel(quizRepository, userStatsRepository, userStatsService)
        vm.startQuiz("PPL", "Met")
        advanceUntilIdle()

        // Select correct for Q1
        vm.selectOption(2)
        vm.nextQuestion()
        // Select incorrect for Q2
        vm.selectOption(0)
        vm.nextQuestion() // finishes
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isQuizFinished)
        assertEquals(1, state.score)
        coVerify { userStatsService.recordStudySession(any(), any(), any(), any(), any(), any(), any()) }
    }
}

