package com.lifeoneuropa.idothecooking.state.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lifeoneuropa.idothecooking.state.db.entities.Category


@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun loadAllCategories(): LiveData<List<Category>>

    @Insert
    suspend fun insert(category: Category): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun bulkInsert(vararg categories: Category): List<Long>

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}