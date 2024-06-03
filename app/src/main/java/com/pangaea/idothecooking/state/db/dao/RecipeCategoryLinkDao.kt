package com.pangaea.idothecooking.state.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink

@Dao
interface RecipeCategoryLinkDao {
    @Insert
    suspend fun insert(recipeCategoryLink: RecipeCategoryLink): Long

    @Query("DELETE FROM recipe_category_links WHERE recipe_id=:id")
    suspend fun deleteAllByRecipe(id: Int)

    @Delete
    suspend fun delete(recipeCategoryLink: RecipeCategoryLink)
}