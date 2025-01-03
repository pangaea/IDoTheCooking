package com.pangaea.idothecooking.ui.shared.adapters

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.RecipeActivity
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.utils.data.JsonAsyncImportTool
import com.pangaea.idothecooking.utils.data.JsonImportTool
import com.pangaea.idothecooking.utils.extensions.observeOnce
import com.pangaea.idothecooking.utils.extensions.readJSONFromAssets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateRecipeAdapter(private val fragment: Fragment, private var viewModel: RecipeViewModel)
    : CreateRecipeCallBackListener {

    fun attemptRecipeInsert(recipe: RecipeDetails) {
        fragment.activity?.runOnUiThread {
            viewModel.getRecipeByName(recipe.recipe.name).observeOnce(fragment) { recipes ->
                if (recipes.isEmpty()) {
                    viewModel.insert(recipe) { id ->
                        val intent = Intent(fragment.activity, RecipeActivity::class.java)
                        val bundle = Bundle()
                        bundle.putInt("id", id.toInt())
                        intent.putExtras(bundle)
                        fragment.requireActivity().startActivity(intent)
                    }
                } else {
                    // Name exists - prompt for a new one
                    NameOnlyDialog(R.string.rename_recipe_before_save, recipe.recipe.name) { name ->
                        recipe.recipe.name = name
                        attemptRecipeInsert(recipe)
                    }.show(fragment.childFragmentManager, null)
                }
            }
        }
    }

    override fun createRecipe(name: String, fileName: String?) {
        if (fileName == null) {
            val recipe = Recipe()
            recipe.name = name
            recipe.description = ""
            attemptRecipeInsert(RecipeDetails(recipe, emptyList(), emptyList(), emptyList()))
        } else {
            // Import template
            val json: String? = fragment.context?.readJSONFromAssets("recipe_templates/${fileName}")
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