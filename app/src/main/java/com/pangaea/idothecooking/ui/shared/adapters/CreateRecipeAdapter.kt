package com.pangaea.idothecooking.ui.shared.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.RecipeActivity
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.utils.data.JsonAsyncImportInterface
import com.pangaea.idothecooking.utils.data.JsonAsyncImportTool
import com.pangaea.idothecooking.utils.extensions.observeOnce
import com.pangaea.idothecooking.utils.extensions.readJSONFromAssets
import com.pangaea.idothecooking.utils.extensions.startActivityWithBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateRecipeAdapter(private val activity: Activity,
                          private val context: Context,
                          private val lifecycleOwner: LifecycleOwner,
                          private val fragmentManager: FragmentManager,
                          private var viewModel: RecipeViewModel,
                          private val callback: ((id: Long) -> Unit)?)
    : CreateRecipeCallBackListener {

    fun attemptRecipeInsert(recipe: RecipeDetails) {
        activity.runOnUiThread {
            viewModel.getRecipeByName(recipe.recipe.name).observeOnce(lifecycleOwner) { recipes ->
                if (recipes.isEmpty()) {
                    // By default - navigate to edit activity
                    viewModel.insert(recipe) { id ->
                        if (callback != null) {
                            callback.invoke(id)
                        } else {
                            activity.startActivityWithBundle(RecipeActivity::class.java, "id", id.toInt())
                        }
                    }
                } else {
                    // Name exists - prompt for a new one
                    NameOnlyDialog(R.string.rename_recipe_before_save, recipe.recipe.name) { name ->
                        recipe.recipe.name = name
                        attemptRecipeInsert(recipe)
                    }.show(fragmentManager, null)
                }
            }
        }
    }

    private fun attemptRecipeInsert(json: String, replacementName: String?, tool: JsonAsyncImportInterface,
                                    ctx: JsonAsyncImportTool.ImportContext) {
        activity.runOnUiThread {
            if (!tool.import(json, replacementName, ctx) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context,
                                       activity.getString(R.string.import_complete),
                                       Toast.LENGTH_LONG).show()
                    }
                }) {
                // Name exists - prompt for a new one
                NameOnlyDialog(R.string.rename_recipe_before_save, replacementName) { name ->
                    attemptRecipeInsert(json, name, tool, ctx)
                }.show(fragmentManager, null)
            }
        }
    }

    override fun isRecipeNameUnique(name: String, callback: (recipe: Recipe?) -> Unit) {
        viewModel.getRecipeByName(name).observeOnce(lifecycleOwner) { recipes ->
            callback(if (recipes.isEmpty()) null else recipes[0])
        }
    }

    override fun createRecipe(name: String, fileName: String?) {
        if (fileName == null) {
            val recipe = Recipe()
            recipe.name = name
            recipe.description = ""
            attemptRecipeInsert(RecipeDetails(recipe, emptyList(), emptyList(), emptyList()))
        } else {
            // Import from template
            val json: String = context.readJSONFromAssets("recipe_templates/${fileName}")
            JsonAsyncImportTool(activity.application, lifecycleOwner).loadData() { tool, ctx ->
                attemptRecipeInsert(json, name, tool, ctx)
            }
        }
    }
}