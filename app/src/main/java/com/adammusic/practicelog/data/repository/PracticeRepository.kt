package com.adammusic.practicelog.data.repository

import com.adammusic.practicelog.data.db.PracticeDatabase
import com.adammusic.practicelog.data.db.entities.CategoryEntity
import com.adammusic.practicelog.data.db.entities.PracticeWithRelations
import com.adammusic.practicelog.data.db.entities.SessionEntity
import com.adammusic.practicelog.data.model.Category
import com.adammusic.practicelog.data.model.Practice
import com.adammusic.practicelog.data.model.PracticeSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class PracticeRepository @Inject constructor(
    private val database: PracticeDatabase
) {
    fun getAllCategories(): Flow<List<Category>> {
        return database.categoryDao().getAllCategories().map { categoryEntities ->
            categoryEntities.map { it.toCategory() }
        }
    }

    fun getAllPractices(): Flow<List<Practice>> {
        return database.practiceDao().getAllPracticesWithRelations().map { practices ->
            practices.map { it.toPractice() }
        }
    }

    fun getPracticeById(id: Int): Flow<Practice?> {
        return database.practiceDao().getPracticeWithRelationsById(id).map { it?.toPractice() }
    }

    private fun CategoryEntity.toCategory() = Category(id, name, color)
    private fun SessionEntity.toSession() = PracticeSession(id, practiceId, date, duration, startingBpm, achievedBpm, notes)

    private fun PracticeWithRelations.toPractice(): Practice {
        return Practice(
            id = practice.id,
            name = practice.name,
            description = practice.description,
            category = category.toCategory(),
            sessions = sessions.map { it.toSession() }
        )
    }

    suspend fun addNewCategory(name: String, color: Long) : Long {
        val category = CategoryEntity(id= 0, name = name, color = color)
        return database.categoryDao().insertCategory(category)
    }

    suspend fun addNewPractice(name: String, description: String, categoryId: Int): Long {
        val practiceEntity = com.adammusic.practicelog.data.db.entities.PracticeEntity(
            id = 0,
            name = name,
            description = description,
            categoryId = categoryId
        )
        return database.practiceDao().insertPractice(practiceEntity)
    }
    
    suspend fun addPracticeSession(
        practiceId: Int,
        duration: Int,
        startingBpm: Int,
        achievedBpm: Int,
        notes: String? = null
    ): Long {
        val sessionEntity = SessionEntity(
            id = 0,
            practiceId = practiceId,
            date = LocalDateTime.now(),
            duration = duration,
            startingBpm = startingBpm,
            achievedBpm = achievedBpm,
            notes = notes
        )
        return database.sessionDao().insertSession(sessionEntity)
    }

    suspend fun updatePractice(
        id: Int,
        name: String,
        description: String,
        categoryId: Int
    ) {
        val practiceEntity = com.adammusic.practicelog.data.db.entities.PracticeEntity(
            id = id,
            name = name,
            description = description,
            categoryId = categoryId
        )
        database.practiceDao().updatePractice(practiceEntity)
    }

    suspend fun getCategoryPracticeCount(categoryId: Int): Int {
        return database.practiceDao().getPracticeCountByCategoryId(categoryId)
    }
    
    suspend fun deleteCategory(categoryId: Int) {
        val category = CategoryEntity(id = categoryId, name = "", color = 0)
        database.categoryDao().deleteCategory(category)
    }

    suspend fun updateSession(
        id: Int,
        practiceId: Int,
        date: LocalDateTime,
        duration: Int,
        startingBpm: Int,
        achievedBpm: Int,
        notes: String?
    ) {
        val sessionEntity = SessionEntity(
            id = id,
            practiceId = practiceId,
            date = date,
            duration = duration,
            startingBpm = startingBpm,
            achievedBpm = achievedBpm,
            notes = notes
        )
        database.sessionDao().updateSession(sessionEntity)
    }
    
    suspend fun deleteSession(sessionId: Int) {
        database.sessionDao().deleteSession(sessionId)
    }
    
    suspend fun deletePractice(practiceId: Int) {

        database.sessionDao().deleteSessionsByPracticeId(practiceId)
        database.practiceDao().deletePractice(practiceId)
    }
}