package com.pangaea.idothecooking.state

import androidx.lifecycle.LiveData
import com.pangaea.idothecooking.state.db.dao.CategoryDao
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Recipe
import java.util.Date
import java.util.Optional
import java.util.function.Consumer

class CategoryRepository(private var categoryDao: CategoryDao) {
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

    fun insertWithTimestamp(o: Category): Category {
        val curTime = System.currentTimeMillis()
        o.createdAt = Date(curTime)
        o.modifiedAt = Date(curTime)
        return o
    }

    fun updateWithTimestamp(o: Category): Category {
        val curTime = System.currentTimeMillis()
        o.modifiedAt = Date(curTime)
        return o
    }
}