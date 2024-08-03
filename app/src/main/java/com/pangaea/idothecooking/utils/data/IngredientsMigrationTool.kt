package com.pangaea.idothecooking.utils.data

import android.app.Application
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.ShoppingListRepository
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.MeasuredItem
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.utils.extensions.observeOnce
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.function.Consumer

class IngredientsMigrationTool(val app: Application, private val lifecycleOwner: LifecycleOwner,
                               private val recipeId: Int, val adjRatio: Double, private val shoppingListId: Int?) {
    private val recipeRepository = RecipeRepository(app)
    private val shoppingListRepository = ShoppingListRepository(app)

    private fun createKey(item: MeasuredItem): String {
        return item.unit.lowercase(Locale.ROOT) + "!" +
                item.name.lowercase(Locale.ROOT)
    }

    private fun isNullOrZero(num: Double?): Boolean {
        return !(num != null && num > 0)
    }

    private fun mergeIngredientsIntoShoppingListItems(shoppingListItems: MutableList<ShoppingListItem>, ingredients: List<Ingredient>) {
        val existingItemsMap: MutableMap<String, ShoppingListItem> = emptyMap<String, ShoppingListItem>().toMutableMap()

        // Iterate existing list items to avoid duplicates
        shoppingListItems.forEach { shoppingListItem: ShoppingListItem ->
            val key = createKey(shoppingListItem)
            existingItemsMap[key] = shoppingListItem
        }
        ingredients.forEach { ingredient: Ingredient ->
            val item = existingItemsMap[createKey(ingredient)]
            if (item != null) {
                if (!isNullOrZero(item.amount) && !isNullOrZero(ingredient.amount)) {
                    item.amount = ingredient.amount?.let { item.amount?.plus((it * adjRatio)) }
                }
            } else {
                val newItem = ShoppingListItem()
                newItem.name = ingredient.name
                newItem.unit = ingredient.unit
                newItem.amount = ingredient.amount?.times(adjRatio)
                shoppingListItems.add(newItem)
            }
        }
    }

    fun mergeShoppingList(shoppingListItems: MutableList<ShoppingListItem>,
                          callback: (shoppingListItems: MutableList<ShoppingListItem>) -> Unit) {

        // Pull ingredients from recipe
        recipeRepository.getRecipeWithDetails(recipeId.toLong()).observeOnce(lifecycleOwner) { recipes ->
            val recipe: RecipeDetails = recipes[0]
            mergeIngredientsIntoShoppingListItems(shoppingListItems, recipe.ingredients)

            // Fire callback with updated list items
            callback(shoppingListItems)
        }
    }

    private fun mergeAndSaveShoppingList(shoppingList: ShoppingListDetails, callback: Consumer<Long>) {
        val itemList: MutableList<ShoppingListItem> = shoppingList.shoppingListItems.toMutableList()
        mergeShoppingList(itemList) {
            // Update items of list and save
            val model =  ShoppingListViewModel(app, null)
            shoppingList.shoppingListItems = itemList
            shoppingList.let {
                if (it.shoppingList.id == 0) {
                    model.insert(it) {o -> callback.accept(o)}
                } else {
                    model.update(it) {o -> callback.accept(o)}
                }
            }
        }
    }

    fun execute(callback: Consumer<Long>) {
        // Query shopping list or create a new one
        if (shoppingListId != null) {
            shoppingListRepository.getShoppingListWithDetails(shoppingListId.toLong()).observeOnce(lifecycleOwner) { shoppingLists ->
                mergeAndSaveShoppingList(shoppingLists[0], callback)
                }
        } else {
            mergeAndSaveShoppingList(ShoppingListDetails(ShoppingList(0, "New List", ""), emptyList()), callback)
        }
    }
}