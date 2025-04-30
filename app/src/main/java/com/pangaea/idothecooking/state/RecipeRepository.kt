package com.pangaea.idothecooking.state

import android.app.Application
import androidx.lifecycle.LiveData
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.converters.TimestampConverter

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

    fun getAllFavoriteRecipesWithDetails(): LiveData<List<RecipeDetails>> {
        return recipeDao.loadAllFavoriteRecipesWithDetails()
    }

    fun getRecipe(id: Long): LiveData<List<Recipe>> {
        return recipeDao.loadRecipesByIds(intArrayOf(id.toInt()))
    }

    fun getRecipeWithDetails(id: Long): LiveData<List<RecipeDetails>> {
        return recipeDao.loadRecipesWithDetailsByIds(intArrayOf(id.toInt()))
    }

    fun getRecipeByName(name: String): LiveData<List<Recipe>> {
        return recipeDao.loadRecipesByNames(arrayOf(name))
    }

    suspend fun insert(recipe: RecipeDetails): Long {
        insertWithTimestamp(recipe.recipe)
        return recipeDao.createAll(directionDao, ingredientDao, recipeCategoryLinkDao, recipe).toLong()
    }

    suspend fun update(recipe: RecipeDetails): Long {
        val tc = TimestampConverter()
        updateWithTimestamp(recipe.recipe)
        return recipeDao.updateAll(directionDao, ingredientDao, recipeCategoryLinkDao, recipe,
                                         tc.dateToTimestamp(recipe.recipe.modifiedAt)).toLong()
    }

    suspend fun update(recipe: Recipe): Long {
        updateWithTimestamp(recipe)
        return recipeDao.update(recipe).toLong()
    }

    suspend fun updateFavorite(recipe: Recipe): Long {
        val tc = TimestampConverter()
        updateWithTimestamp(recipe)
        return recipeDao.updateFavorite(recipe.id.toLong(), tc.dateToTimestamp(recipe.modifiedAt),
                                        recipe.favorite).toLong()
    }

    suspend fun delete(recipe: Recipe) {
        recipeDao.delete(recipe)
    }
}