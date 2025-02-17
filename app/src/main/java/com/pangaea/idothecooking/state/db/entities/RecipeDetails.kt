package com.pangaea.idothecooking.state.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

data class RecipeDetails (
    @Embedded
    var recipe: Recipe,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    var ingredients: List<Ingredient>,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    var directions: List<Direction>,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    var categories: List<RecipeCategoryLink>

//    fun getDirectionsSorted() {
//        return directions.sortWith(Comparator { obj1, obj2 -> // ## Ascending order
//            Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
//        }
//    })
)