package com.pilotprep.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "offline_quiz")
data class OfflineQuiz(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val questions: String, // JSON string
    val isEssential: Boolean = false,
    val downloadedAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis(),
    val accessCount: Int = 0
)

@Entity(tableName = "offline_quiz_result")
data class OfflineQuizResult(
    @PrimaryKey val id: String,
    val quizId: String,
    val userId: String,
    val score: Int,
    val totalQuestions: Int,
    val answers: String, // JSON string
    val completedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Dao
interface OfflineQuizDao {
    @Query("SELECT * FROM offline_quiz WHERE isEssential = 1 ORDER BY lastAccessed DESC")
    fun getEssentialQuizzes(): Flow<List<OfflineQuiz>>
    
    @Query("SELECT * FROM offline_quiz WHERE category = :category ORDER BY lastAccessed DESC")
    fun getQuizzesByCategory(category: String): Flow<List<OfflineQuiz>>
    
    @Query("SELECT * FROM offline_quiz WHERE id = :quizId")
    suspend fun getQuizById(quizId: String): OfflineQuiz?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: OfflineQuiz)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<OfflineQuiz>)
    
    @Update
    suspend fun updateQuiz(quiz: OfflineQuiz)
    
    @Delete
    suspend fun deleteQuiz(quiz: OfflineQuiz)
    
    @Query("DELETE FROM offline_quiz WHERE downloadedAt < :cutoffTime")
    suspend fun deleteOldQuizzes(cutoffTime: Long)
    
    @Query("SELECT COUNT(*) FROM offline_quiz")
    suspend fun getQuizCount(): Int
    
    @Query("SELECT SUM(LENGTH(questions)) FROM offline_quiz")
    suspend fun getTotalStorageUsed(): Long
}

@Dao
interface OfflineQuizResultDao {
    @Query("SELECT * FROM offline_quiz_result WHERE userId = :userId ORDER BY completedAt DESC")
    fun getUserResults(userId: String): Flow<List<OfflineQuizResult>>
    
    @Query("SELECT * FROM offline_quiz_result WHERE isSynced = 0")
    suspend fun getUnsyncedResults(): List<OfflineQuizResult>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: OfflineQuizResult)
    
    @Update
    suspend fun updateResult(result: OfflineQuizResult)
    
    @Query("UPDATE offline_quiz_result SET isSynced = 1 WHERE id = :resultId")
    suspend fun markAsSynced(resultId: String)
    
    @Query("DELETE FROM offline_quiz_result WHERE completedAt < :cutoffTime")
    suspend fun deleteOldResults(cutoffTime: Long)
}



