package com.pangaea.idothecooking.utils.data

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.data.JsonImportTool.MessageType
import com.pangaea.idothecooking.utils.data.JsonImportTool.ParseLog
import com.pangaea.idothecooking.utils.extensions.observeOnce
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Optional
import java.util.function.Consumer

class JsonAsyncImportTool(val app: Application, private val lifecycleOwner: LifecycleOwner): JsonAsyncImportInterface {
    data class ImportContext(var categoryMap: MutableMap<String, Int>,
                             var recipeMap: MutableMap<String, Int>,
                             var shoppingListMap: MutableMap<String, Int>)

    fun loadData(callback: (callback: JsonAsyncImportInterface, importContext: ImportContext) -> Unit) {
        val importContext = ImportContext(emptyMap<String, Int>().toMutableMap(),
                                          emptyMap<String, Int>().toMutableMap(),
                                          emptyMap<String, Int>().toMutableMap())
        val categoryViewModel = CategoryViewModelFactory(app, null).create(CategoryViewModel::class.java)
        val recipeViewModel = RecipeViewModelFactory(app, null).create(RecipeViewModel::class.java)
        val shoppingListViewModel = ShoppingListViewModelFactory(app,null).create(ShoppingListViewModel::class.java)
        categoryViewModel.getAllCategories().observeOnce(lifecycleOwner) { categories ->
            importContext.categoryMap = categories.associateBy({ it.name }, { it.id }).toMutableMap()
            recipeViewModel.getAllRecipes().observeOnce(lifecycleOwner) { recipes ->
                importContext.recipeMap = recipes.associateBy({ it.name }, { it.id }).toMutableMap()
                shoppingListViewModel.getAllShoppingLists().observeOnce(lifecycleOwner) { shoppingLists ->
                    importContext.shoppingListMap = shoppingLists.associateBy({ it.name }, { it.id }).toMutableMap()
                    callback(this, importContext)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(DelicateCoroutinesApi::class)
    private fun launchImportTool(json: String, replacementName: String?, ctx: ImportContext,
                                 callback: Consumer<List<JsonImportTool.ParseLog>>) {
        GlobalScope.launch {
            try {
                val errs = JsonImportTool(app, replacementName,
                                          ctx.categoryMap,
                                          ctx.recipeMap,
                                          ctx.shoppingListMap).import(json)
                Optional.ofNullable(callback)
                    .ifPresent { o: Consumer<List<JsonImportTool.ParseLog>> ->
                        o.accept(errs)
                    }
            } catch (e: Exception) {
                callback.accept(listOf(ParseLog(MessageType.ERROR, app.getString(R.string.import_error_invalid_file))))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun import(json: String, replacementName: String?, ctx: ImportContext,
                        callback: Consumer<List<JsonImportTool.ParseLog>>): Boolean {
        if (!ctx.recipeMap.contains(replacementName)) {
            launchImportTool(json, replacementName, ctx, callback)
            return true
        }
        return false
    }
}