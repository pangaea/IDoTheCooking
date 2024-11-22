package com.pangaea.idothecooking.ui.shared.adapters

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.RecipeActivity
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.utils.data.JsonAsyncImportTool
import com.pangaea.idothecooking.utils.data.JsonImportTool
import com.pangaea.idothecooking.utils.extensions.readJSONFromAssets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateRecipeAdapter(private val fragment: Fragment, private var viewModel: RecipeViewModel)
    : CreateRecipeCallBackListener {
    override fun createRecipe(name: String, fileName: String?) {
        if (fileName == null) {
            val recipe = Recipe()
            recipe.name = name
            recipe.description = ""
            val details = RecipeDetails(recipe, emptyList(), emptyList(), emptyList())
            viewModel.insert(details) { id: Long ->
                val recipeIntent = Intent(fragment.activity, RecipeActivity::class.java)
                val bundle = Bundle()
                bundle.putInt("id", id.toInt())
                recipeIntent.putExtras(bundle)
                fragment.startActivity(recipeIntent)
            }
        } else {
            // Import template
            val json: String? = fragment.context?.let { it.readJSONFromAssets("recipe_templates/${fileName}") }
            if (json != null) {
                JsonAsyncImportTool(fragment.requireActivity().application, name, fragment).import(json){
                    CoroutineScope(Dispatchers.Main).launch {
                        if (it.isEmpty()) {
                            Toast.makeText(fragment.context,
                                           fragment.getString(R.string.import_complete),
                                           Toast.LENGTH_LONG).show()
                        } else {
                            val errs: String = it.map{
                                it.message
                            }.joinToString { it -> it }
                            Toast.makeText(fragment.context,
                                           errs,
                                           Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}