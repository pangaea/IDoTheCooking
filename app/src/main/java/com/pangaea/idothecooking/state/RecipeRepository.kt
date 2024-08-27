package com.pangaea.idothecooking.state

import android.app.Application
import androidx.lifecycle.LiveData
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.dao.CategoryDao
import com.pangaea.idothecooking.state.db.dao.RecipeCategoryLinkDao
import com.pangaea.idothecooking.state.db.dao.RecipeDao
import com.pangaea.idothecooking.state.db.dao.RecipeDirectionDao
import com.pangaea.idothecooking.state.db.dao.RecipeIngredientDao
import com.pangaea.idothecooking.state.db.dao.ShoppingListDao
import com.pangaea.idothecooking.state.db.dao.ShoppingListItemDao
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.converters.TimestampConverter
import java.util.Date
import java.util.Optional
import java.util.function.Consumer

class RecipeRepository(application: Application) : RepositoryBase<Recipe>() {

    val db = (application as IDoTheCookingApp).getDatabase()
    private val recipeDao = db.recipeDao()!!
    private val directionDao = db.recipeDirectionDao()!!
    private val ingredientDao = db.recipeIngredientDao()!!
    private val recipeCategoryLinkDao = db.recipeCategoryLinkDao()!!

    fun getAllRecipes(): LiveData<List<Recipe>> {
        return recipeDao.loadAllRecipes()
    }
    fun getAllRecipesWithDetails(): LiveData<List<RecipeDetails>> {
        return recipeDao.loadAllRecipesWithDetails()
    }

    fun getRecipe(id: Long): LiveData<List<Recipe>> {
        return recipeDao.loadRecipesByIds(intArrayOf(id.toInt()))
    }

    fun getRecipeWithDetails(id: Long): LiveData<List<RecipeDetails>> {
        return recipeDao.loadRecipesWithDetailsByIds(intArrayOf(id.toInt()))
    }

    suspend fun insert(recipe: RecipeDetails): Long {
        val id: Long = recipeDao.insert(insertWithTimestamp(recipe.recipe))
        insertDirections(recipe.directions, id.toInt())
        insertIngredients(recipe.ingredients, id.toInt())
        insertCategoryLinks(recipe.categories, id.toInt())
        return id
    }

    suspend fun update(recipe: RecipeDetails): Long {
        val tc = TimestampConverter()
        updateWithTimestamp(recipe.recipe)
        val id: Int = recipeDao.updateData(recipe.recipe.id.toLong(), recipe.recipe.name, recipe.recipe.description,
            recipe.recipe.imageUri, recipe.recipe.servings, tc.dateToTimestamp(recipe.recipe.modifiedAt))
        directionDao.deleteAllByRecipe(recipe.recipe.id)
        insertDirections(recipe.directions, recipe.recipe.id)
        ingredientDao.deleteAllByRecipe(recipe.recipe.id)
        insertIngredients(recipe.ingredients, recipe.recipe.id)
        recipeCategoryLinkDao.deleteAllByRecipe(recipe.recipe.id)
        insertCategoryLinks(recipe.categories, recipe.recipe.id)
        return id.toLong();
    }

//    private fun updateRecipeDate(recipe: RecipeDetails): RecipeDetails? {
//        updateWithTimestamp(recipe.recipe)
//        return recipe
//    }

    suspend fun delete(recipe: Recipe) {
        recipeDao.delete(recipe)
    }

    private suspend fun insertDirections(items: List<Direction>, idNew: Int) {
        var i = 0
        val size = items.size
        while (i < size) {
            val item: Direction = items[i]
            item.recipe_id = idNew
            item.order = i
            directionDao.insert(item)
            i++
        }
    }

    private suspend fun insertIngredients(items: List<Ingredient>, idNew: Int) {
        var i = 0
        val size = items.size
        while (i < size) {
            val item: Ingredient = items[i]
            item.recipe_id = idNew
            item.order = i
            ingredientDao.insert(item)
            i++
        }
    }

    private suspend fun insertCategoryLinks(items: List<RecipeCategoryLink>, idNew: Int) {
        var i = 0
        val size = items.size
        while (i < size) {
            val item: RecipeCategoryLink = items[i]
            item.recipe_id = idNew
            recipeCategoryLinkDao.insert(item)
            i++
        }
    }
}