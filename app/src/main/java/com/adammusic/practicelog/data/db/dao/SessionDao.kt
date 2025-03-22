package com.adammusic.practicelog.data.db.dao

import androidx.room.*
import androidx.room.Query
import androidx.room.Transaction
import com.adammusic.practicelog.data.db.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Transaction
    @Query("SELECT * FROM sessions WHERE practiceId = :practiceId ORDER BY date DESC")
    fun getSessionsByPracticeId(practiceId: Int): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Int)

    @Query("DELETE FROM sessions WHERE practiceId = :practiceId")
    suspend fun deleteSessionsByPracticeId(practiceId: Int)
}