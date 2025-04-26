package com.lifeoneuropa.idothecooking.ui.shared.adapters

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.state.db.entities.Recipe
import com.lifeoneuropa.idothecooking.state.db.entities.RecipeDetails
import com.lifeoneuropa.idothecooking.ui.recipe.RecipeActivity
import com.lifeoneuropa.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.lifeoneuropa.idothecooking.ui.shared.NameOnlyDialog
import com.lifeoneuropa.idothecooking.utils.data.JsonAsyncImportInterface
import com.lifeoneuropa.idothecooking.utils.data.JsonAsyncImportTool
import com.lifeoneuropa.idothecooking.utils.data.JsonImportTool
import com.lifeoneuropa.idothecooking.utils.extensions.observeOnce
import com.lifeoneuropa.idothecooking.utils.extensions.readContentFromAssets
import com.lifeoneuropa.idothecooking.utils.extensions.startActivityWithBundle
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

    @RequiresApi(Build.VERSION_CODES.N)
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

    private fun getFirstRecipeId(logs: List<JsonImportTool.ParseLog>): Int {
        val stats = logs.find { it.type == JsonImportTool.MessageType.STATS }
        if (stats != null) {
            val mapper = ObjectMapper()
            val node: JsonNode = mapper.readTree(stats.message)
            val recipeIdsNode: JsonNode? = node.get("newRecipeIds")
            if (recipeIdsNode != null && recipeIdsNode.isArray) {
                return recipeIdsNode[0].intValue()
            }
        }
        return 0
    }

    private fun attemptRecipeInsert(json: String, replacementName: String?, tool: JsonAsyncImportInterface,
                                    ctx: JsonAsyncImportTool.ImportContext) {
        activity.runOnUiThread {
            if (!tool.import(json, replacementName, ctx) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val recipeId = getFirstRecipeId(it)
                        if (recipeId > 0) {
                            if (callback != null) {
                                callback.invoke(recipeId.toLong())
                            } else {
                                activity.startActivityWithBundle(RecipeActivity::class.java,
                                                                 "id",
                                                                 recipeId.toInt())
                            }
                        } else {
                            Toast.makeText(context,
                                           activity.getString(R.string.import_complete),
                                           Toast.LENGTH_LONG).show()
                        }
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun createRecipe(name: String, fileName: String?) {
        if (fileName == null) {
            val recipe = Recipe()
            recipe.name = name
            recipe.description = ""
            attemptRecipeInsert(RecipeDetails(recipe, emptyList(), emptyList(), emptyList()))
        } else {
            // Import from template
            val json: String = context.readContentFromAssets("recipe_templates/${fileName}")
            JsonAsyncImportTool(activity.application, lifecycleOwner).loadData() { tool, ctx ->
                attemptRecipeInsert(json, name, tool, ctx)
            }
        }
    }
}