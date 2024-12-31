package com.pangaea.idothecooking.utils.data

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.extensions.observeOnce
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Optional
import java.util.function.Consumer

class JsonAsyncImportTool(val app: Application, private var replaceName: String?, private val lifecycleOwner: LifecycleOwner) {
    data class ImportContext(var categoryMap: MutableMap<String, Int>,
                             var recipeMap: MutableMap<String, Int>,
                             var shoppingListMap: MutableMap<String, Int>)

    private fun loadData(callback: (importContext: ImportContext) -> Unit) {
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
                shoppingListViewModel.getAllShoppingLists()
                    .observeOnce(lifecycleOwner) { shoppingLists ->
                        importContext.shoppingListMap = shoppingLists.associateBy({ it.name }, { it.id }).toMutableMap()
                        callback(importContext)
                    }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun import(json: String, callback: Consumer<List<JsonImportTool.ParseLog>>) {
        val messages: MutableList<JsonImportTool.ParseLog> = emptyList<JsonImportTool.ParseLog>().toMutableList()
        try {
            loadData() { cxt ->
                GlobalScope.launch {
                    val errs = JsonImportTool(app, replaceName,
                                              cxt.categoryMap,
                                              cxt.recipeMap,
                                              cxt.shoppingListMap).import(json)
                    Optional.ofNullable(callback)
                        .ifPresent { o: Consumer<List<JsonImportTool.ParseLog>> ->
                            o.accept(errs)
                        }
                }
            }
        } catch(e: Exception) {
            messages.add(JsonImportTool.ParseLog(JsonImportTool.MessageType.ERROR,
                                                 e.message.let{e.message} ?: "Error loading data"))
        }
    }
}