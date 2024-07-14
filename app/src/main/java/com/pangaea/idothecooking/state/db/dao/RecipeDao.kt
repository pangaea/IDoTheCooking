package com.pangaea.idothecooking.state.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY modified_at COLLATE NOCASE DESC")
    fun loadAllRecipes(): LiveData<List<Recipe>>

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY modified_at COLLATE NOCASE DESC")
    fun loadAllRecipesWithDetails(): LiveData<List<RecipeDetails>>

    @Query("SELECT * FROM recipes WHERE id IN (:recipeIds)")
    fun loadRecipesByIds(recipeIds: IntArray): LiveData<List<Recipe>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id IN (:recipeIds) order by modified_at COLLATE NOCASE desc")
    fun loadRecipesWithDetailsByIds(recipeIds: IntArray): LiveData<List<RecipeDetails>>

    @Insert
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Query("UPDATE recipes SET name = :name, description = :description, imageUri = :imageUri," +
            "servings = :servings, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateData(id: Long, name: String?, description: String?, imageUri: String?,
                           servings: Int?, modifiedAt: String?): Int

    @Delete
    suspend fun delete(recipe: Recipe)
}