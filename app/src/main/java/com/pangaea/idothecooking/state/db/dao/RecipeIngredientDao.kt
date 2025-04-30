package com.pangaea.idothecooking.state.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pangaea.idothecooking.state.db.entities.Ingredient

@Dao
interface RecipeIngredientDao {
    @Insert
    suspend fun insert(ingredients: Ingredient)

    @Query("DELETE FROM ingredients WHERE recipe_id=:id")
    suspend fun deleteAllByRecipe(id: Int)
}