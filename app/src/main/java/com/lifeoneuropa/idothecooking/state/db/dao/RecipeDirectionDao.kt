package com.lifeoneuropa.idothecooking.state.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lifeoneuropa.idothecooking.state.db.entities.Direction

@Dao
interface RecipeDirectionDao {
    @Insert
    suspend fun insert(directions: Direction)

    @Query("DELETE FROM directions WHERE recipe_id=:id")
    suspend fun deleteAllByRecipe(id: Int)
}