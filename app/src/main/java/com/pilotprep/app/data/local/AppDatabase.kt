package com.pilotprep.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [QuestionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pilot_prep_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val database = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            prePopulateDatabase(context, database.questionDao())
        }
    }

    private suspend fun prePopulateDatabase(context: Context, questionDao: QuestionDao) {
        if (questionDao.count() == 0) {
            try {
                val jsonString = context.assets.open("questions.json").bufferedReader().use { it.readText() }
                val gson = Gson()
                val questionListType = object : TypeToken<List<QuestionEntity>>() {}.type
                val questions: List<QuestionEntity> = gson.fromJson(jsonString, questionListType)
                questionDao.insertAll(questions)
            } catch (e: Exception) {
                // Log the exception
                e.printStackTrace()
            }
        }
    }
}