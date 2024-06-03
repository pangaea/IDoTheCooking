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
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.converters.TimestampConverter
import java.util.Date
import java.util.Optional
import java.util.function.Consumer

class RecipeRepository(private var recipeDao: RecipeDao) {

    var db: AppDatabase? = null
    private var mDirectionDao: RecipeDirectionDao? = null
    private var mIngredientDao: RecipeIngredientDao? = null
    private var recipeCategoryLinkDao: RecipeCategoryLinkDao? = null

    constructor(
        recipeDao: RecipeDao,
        directionDao: RecipeDirectionDao,
        ingredientDao: RecipeIngredientDao,
        categoryLinkDao: RecipeCategoryLinkDao
    ) : this(recipeDao) {
        mDirectionDao = directionDao
        mIngredientDao = ingredientDao
        recipeCategoryLinkDao = categoryLinkDao
    }

    fun init(application: Application) {
        db = (application as IDoTheCookingApp).getDatabase()
        mDirectionDao = db!!.recipeDirectionDao()
        mIngredientDao = db!!.recipeIngredientDao()
        recipeCategoryLinkDao = db!!.recipeCategoryLinkDao()
    }
    //val allRecipes: LiveData<List<Recipe>> = recipeDao.loadAllRecipes()
    fun getAllRecipes(): LiveData<List<Recipe>> {
        return recipeDao.loadAllRecipes()
    }
    fun getAllRecipesWithDetails(): LiveData<List<RecipeDetails>> {
        return recipeDao.loadAllRecipesWithDetails()
    }
//    val allRecipes: () -> LiveData<List<Recipe>>
//        get() = {
//            recipeDao.loadAllRecipes()
//    }

    fun getRecipe(id: Long): LiveData<List<Recipe>> {
        return recipeDao.loadAllByIds(intArrayOf(id.toInt()))
    }

    fun getRecipeWithDetails(id: Long): LiveData<List<RecipeDetails>> {
        return recipeDao.getRecipeWithDetailsByIds(intArrayOf(id.toInt()))
    }

    suspend fun insert(recipe: RecipeDetails, callback: Consumer<Long>?) {
        val id: Long = recipeDao.insert(insertWithTimestamp(recipe.recipe))
        insertDirections(recipe.directions, id.toInt())
        insertIngredients(recipe.ingredients, id.toInt())
        insertCategoryLinks(recipe.categories, id.toInt())
        Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
            o.accept(id)
        }
    }

    suspend fun update(recipe: RecipeDetails) {
        val tc = TimestampConverter()
        updateWithTimestamp(recipe.recipe)
        recipeDao.updateData(recipe.recipe.id.toLong(), recipe.recipe.name, recipe.recipe.description,
            recipe.recipe.imageUri, recipe.recipe.servings, tc.dateToTimestamp(recipe.recipe.modifiedAt))
        mDirectionDao?.deleteAllByRecipe(recipe.recipe.id)
        insertDirections(recipe.directions, recipe.recipe.id)
        mIngredientDao?.deleteAllByRecipe(recipe.recipe.id)
        insertIngredients(recipe.ingredients, recipe.recipe.id)

        recipeCategoryLinkDao?.deleteAllByRecipe(recipe.recipe.id)
        insertCategoryLinks(recipe.categories, recipe.recipe.id)
    }

    private fun updateRecipeDate(recipe: RecipeDetails): RecipeDetails? {
        updateWithTimestamp(recipe.recipe)
        return recipe
    }

    suspend fun delete(recipe: Recipe) {
        recipeDao.delete(recipe)
    }

    fun insertWithTimestamp(o: Recipe): Recipe {
        val curTime = System.currentTimeMillis()
        o.createdAt = Date(curTime)
        o.modifiedAt = Date(curTime)
        return o
    }

    fun updateWithTimestamp(o: Recipe): Recipe {
        val curTime = System.currentTimeMillis()
        o.modifiedAt = Date(curTime)
        return o
    }

    private suspend fun insertDirections(items: List<Direction>, idNew: Int) {
        var i = 0
        val _size = items.size
        while (i < _size) {
            val item: Direction = items[i]
            item.recipe_id = idNew
            mDirectionDao?.insert(item)
            i++
        }
    }

    private suspend fun insertIngredients(items: List<Ingredient>, idNew: Int) {
        var i = 0
        val _size = items.size
        while (i < _size) {
            val item: Ingredient = items[i]
            item.recipe_id = idNew
            mIngredientDao?.insert(item)
            i++
        }
    }

    private suspend fun insertCategoryLinks(items: List<RecipeCategoryLink>, idNew: Int) {
        var i = 0
        val _size = items.size
        while (i < _size) {
            val item: RecipeCategoryLink = items[i]
            item.recipe_id = idNew
            recipeCategoryLinkDao?.insert(item)
            i++
        }
    }
}