package com.pangaea.idothecooking.state

import android.app.Application
import androidx.lifecycle.LiveData
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink

class CategoryRepository(application: Application) : RepositoryBase<Category>() {

    val db = (application as IDoTheCookingApp).getDatabase()
    private val categoryDao = db.categoryDao()!!
    private val recipeCategoryLinkDao = db.recipeCategoryLinkDao()!!

    fun getAllCategories(): LiveData<List<Category>> {
        return categoryDao.loadAllCategories()
    }

    fun getAllRecipeLinks(id: Int): LiveData<List<RecipeCategoryLink>> {
        return recipeCategoryLinkDao.loadRecipeLinksByCategory(intArrayOf(id))
    }

    suspend fun insert(category: Category): Long {
        return categoryDao.insert(insertWithTimestamp(category))
    }

    suspend fun bulkInsert(categories: List<Category>): List<Long> {
        return categoryDao.bulkInsert(*bulkInsertWithTimestamps(categories).toTypedArray())
    }

    suspend fun update(category: Category) {
        categoryDao.update(insertWithTimestamp(category))
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }
}