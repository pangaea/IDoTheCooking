package com.pangaea.idothecooking.ui.shared.adapters

import com.pangaea.idothecooking.state.db.entities.RecipeDetails

interface CreateRecipeCallBackListener {
    fun createRecipe(name: String, fileName: String?)
}