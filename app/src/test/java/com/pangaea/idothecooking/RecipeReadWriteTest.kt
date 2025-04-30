package com.pangaea.idothecooking

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.rule.ActivityTestRule
//import androidx.test.internal.runner.
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.dao.RecipeDao
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
//import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner

@RunWith(AndroidJUnit4::class)
class RecipeReadWriteTest {
    private lateinit var recipeDao: RecipeDao
    private lateinit var db: AppDatabase

//    @get:Rule
//    val activityRule = ActivityTestRule(MainActivity::class.java)


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        recipeDao = db.recipeDao()!!
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
//        val recipe: Recipe = Recipe()
//        recipe.name = "Test"
//        recipe.description = "Test desc."
//        recipeDao.insert(recipe)
//        val z: List<RecipeDetails> = recipeDao.loadRecipeDetails()
//        assert(z.isNotEmpty())
//        val user: User = TestUtil.createUser(3).apply {
//            setName("george")
//        }
//        userDao.insert(user)
//        val byName = userDao.findUsersByName("george")
//        assertThat(byName.get(0), equalTo(user))
    }
}