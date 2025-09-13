package com.pilotprep.app.data

import com.pilotprep.app.data.local.QuestionDao
import com.pilotprep.app.data.local.QuestionEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(private val questionDao: QuestionDao) {

    suspend fun getSubjects(license: String): List<String> {
        return questionDao.getSubjectsForLicense(license)
    }

    suspend fun getQuizQuestions(license: String, subject: String, count: Int): List<QuestionEntity> {
        return questionDao.getQuestionsForQuiz(license, subject, count)
    }
}