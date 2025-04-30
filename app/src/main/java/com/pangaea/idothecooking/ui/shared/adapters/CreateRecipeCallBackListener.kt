package com.pangaea.idothecooking.ui.shared.adapters

import com.pangaea.idothecooking.state.db.entities.Recipe

interface CreateRecipeCallBackListener {
    fun createRecipe(name: String, fileName: String?)
    fun isRecipeNameUnique(name: String, callback: (recipe: Recipe?) -> Unit)
}