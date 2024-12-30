package com.pangaea.idothecooking.state.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.converters.TimestampConverter

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY modified_at COLLATE NOCASE DESC")
    fun loadAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes ORDER BY modified_at COLLATE NOCASE DESC")
    fun loadAllRecipesWithDetails(): LiveData<List<RecipeDetails>>

    @Query("SELECT * FROM recipes WHERE id IN (:recipeIds)")
    fun loadRecipesByIds(recipeIds: IntArray): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id IN (:recipeIds) order by modified_at COLLATE NOCASE desc")
    fun loadRecipesWithDetailsByIds(recipeIds: IntArray): LiveData<List<RecipeDetails>>

    @Query("SELECT * FROM recipes WHERE name IN (:recipeNames)")
    fun loadRecipesByNames(recipeNames: Array<String>): LiveData<List<Recipe>>

    @Insert
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Query("UPDATE recipes SET name = :name, description = :description, imageUri = :imageUri," +
            "servings = :servings, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateData(id: Long, name: String?, description: String?, imageUri: String?,
                           servings: Int?, modifiedAt: String?): Int

    @Transaction
    suspend fun createAll(directionDao: RecipeDirectionDao, ingredientDao: RecipeIngredientDao,
                          recipeCategoryLinkDao: RecipeCategoryLinkDao, recipe: RecipeDetails): Int {
        val id: Long = insert(recipe.recipe)
        insertDirections(directionDao, recipe.directions, id.toInt())
        insertIngredients(ingredientDao, recipe.ingredients, id.toInt())
        insertCategoryLinks(recipeCategoryLinkDao, recipe.categories, id.toInt())
        return id.toInt()
    }
    @Transaction
    suspend fun updateAll(directionDao: RecipeDirectionDao, ingredientDao: RecipeIngredientDao,
                          recipeCategoryLinkDao: RecipeCategoryLinkDao, recipe: RecipeDetails, modifiedAt: String?): Int {
        val id: Int = updateData(recipe.recipe.id.toLong(), recipe.recipe.name, recipe.recipe.description,
                                           recipe.recipe.imageUri, recipe.recipe.servings, modifiedAt)
        directionDao.deleteAllByRecipe(recipe.recipe.id)
        insertDirections(directionDao, recipe.directions, recipe.recipe.id)
        ingredientDao.deleteAllByRecipe(recipe.recipe.id)
        insertIngredients(ingredientDao, recipe.ingredients, recipe.recipe.id)
        recipeCategoryLinkDao.deleteAllByRecipe(recipe.recipe.id)
        insertCategoryLinks(recipeCategoryLinkDao, recipe.categories, recipe.recipe.id)
        return id
    }

    @Delete
    suspend fun delete(recipe: Recipe)

    private suspend fun insertDirections(directionDao: RecipeDirectionDao,
                                         items: List<Direction>, idNew: Int) {
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

    private suspend fun insertIngredients(ingredientDao: RecipeIngredientDao,
                                          items: List<Ingredient>, idNew: Int) {
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

    private suspend fun insertCategoryLinks(recipeCategoryLinkDao: RecipeCategoryLinkDao,
                                            items: List<RecipeCategoryLink>, idNew: Int) {
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