package com.pangaea.idothecooking.state

import android.app.Application
import androidx.lifecycle.LiveData
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.db.dao.CategoryDao
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Recipe
import java.util.Date
import java.util.Optional
import java.util.function.Consumer

class CategoryRepository(application: Application) : RepositoryBase<Category>() {

    val db = (application as IDoTheCookingApp).getDatabase()
    private val categoryDao = db.categoryDao()!!
    fun getAllCategories(): LiveData<List<Category>> {
        return categoryDao.loadAllCategories()
    }

    suspend fun insert(category: Category, callback: Consumer<Long>?) {
        val id: Long = categoryDao.insert(insertWithTimestamp(category))
        Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
            o.accept(id)
        }
    }

    suspend fun update(category: Category) {
        categoryDao.update(insertWithTimestamp(category))
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }
}