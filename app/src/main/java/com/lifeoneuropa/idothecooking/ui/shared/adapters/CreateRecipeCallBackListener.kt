package com.lifeoneuropa.idothecooking.ui.shared.adapters

import com.lifeoneuropa.idothecooking.state.db.entities.Recipe

interface CreateRecipeCallBackListener {
    fun createRecipe(name: String, fileName: String?)
    fun isRecipeNameUnique(name: String, callback: (recipe: Recipe?) -> Unit)
}