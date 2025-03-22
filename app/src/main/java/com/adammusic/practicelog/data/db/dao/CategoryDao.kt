package com.adammusic.practicelog.data.db.dao

import androidx.room.*
import com.adammusic.practicelog.data.db.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity) : Long

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}