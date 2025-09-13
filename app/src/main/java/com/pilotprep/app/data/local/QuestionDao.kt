package com.pilotprep.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions WHERE license = :license AND subject = :subject ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsForQuiz(license: String, subject: String, limit: Int): List<QuestionEntity>

    @Query("SELECT DISTINCT subject FROM questions WHERE license = :license")
    suspend fun getSubjectsForLicense(license: String): List<String>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int
}