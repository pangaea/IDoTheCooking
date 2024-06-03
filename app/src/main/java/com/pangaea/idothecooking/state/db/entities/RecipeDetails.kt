package com.pangaea.idothecooking.state.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

data class RecipeDetails (
    @Embedded
    val recipe: Recipe,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    var ingredients: List<Ingredient>,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    var directions: List<Direction>,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    var categories: List<RecipeCategoryLink>
)