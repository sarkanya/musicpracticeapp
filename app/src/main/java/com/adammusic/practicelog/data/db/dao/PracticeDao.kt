package com.adammusic.practicelog.data.db.dao

import androidx.room.*
import com.adammusic.practicelog.data.db.entities.PracticeEntity
import com.adammusic.practicelog.data.db.entities.PracticeWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {
    @Transaction
    @Query("SELECT * FROM practices")
    fun getAllPracticesWithRelations(): Flow<List<PracticeWithRelations>>

    @Transaction
    @Query("SELECT * FROM practices WHERE id = :practiceId")
    fun getPracticeWithRelationsById(practiceId: Int): Flow<PracticeWithRelations?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPractice(practice: PracticeEntity): Long

    @Update
    suspend fun updatePractice(practice: PracticeEntity)

    @Query("DELETE FROM practices WHERE id = :practiceId")
    suspend fun deletePractice(practiceId: Int)

    @Query("SELECT COUNT(*) FROM practices WHERE categoryId = :categoryId")
    suspend fun getPracticeCountByCategoryId(categoryId: Int): Int
}