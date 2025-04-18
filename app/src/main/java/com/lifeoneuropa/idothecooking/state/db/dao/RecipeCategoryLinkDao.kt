package com.lifeoneuropa.idothecooking.state.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lifeoneuropa.idothecooking.state.db.entities.RecipeCategoryLink

@Dao
interface RecipeCategoryLinkDao {

    @Query("SELECT * FROM recipe_category_links WHERE category_id IN (:categoryIds)")
    fun loadRecipeLinksByCategory(categoryIds: IntArray): LiveData<List<RecipeCategoryLink>>

    @Insert
    suspend fun insert(recipeCategoryLink: RecipeCategoryLink): Long

    @Query("DELETE FROM recipe_category_links WHERE recipe_id=:id")
    suspend fun deleteAllByRecipe(id: Int)

    @Delete
    suspend fun delete(recipeCategoryLink: RecipeCategoryLink)
}