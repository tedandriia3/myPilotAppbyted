package com.pilotprep.app.viewmodels

import com.pilotprep.app.data.model.Question
import com.pilotprep.app.data.repository.QuizRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExamViewModelTest {

    @Test
    fun startExam_loadsQuestionsAndStopsLoading() = runTest(StandardTestDispatcher()) {
        val quizRepository = mockk<QuizRepository>()
        val fakeQuestions = List(5) { index ->
            Question(
                id = "q$index",
                license = "CPL",
                subject = "Navigation",
                questionText = "Question $index?",
                options = listOf("A", "B", "C", "D"),
                correctOptionIndex = 1
            )
        }
        coEvery { quizRepository.getQuestionsForExam(any(), any(), any()) } returns Result.success(fakeQuestions)

        val viewModel = ExamViewModel(quizRepository)

        viewModel.startExam(license = "CPL", subject = "Navigation")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(5, state.questions.size)
    }
}

