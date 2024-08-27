package com.pangaea.idothecooking.state.db

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.dao.RecipeDao
import com.pangaea.idothecooking.state.db.dao.RecipeDirectionDao
import com.pangaea.idothecooking.state.db.dao.RecipeIngredientDao
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.CategoryRepository
import com.pangaea.idothecooking.state.ShoppingListRepository
import com.pangaea.idothecooking.state.db.dao.CategoryDao
import com.pangaea.idothecooking.state.db.dao.RecipeCategoryLinkDao
import com.pangaea.idothecooking.state.db.dao.ShoppingListDao
import com.pangaea.idothecooking.state.db.dao.ShoppingListItemDao
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem
import com.pangaea.idothecooking.utils.data.JsonImportTool
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(version = 1, entities = [Recipe::class, Ingredient::class, Direction::class,
    Category::class, RecipeCategoryLink::class, ShoppingList::class, ShoppingListItem::class], exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao?
    abstract fun recipeDirectionDao(): RecipeDirectionDao?
    abstract fun recipeIngredientDao(): RecipeIngredientDao?
    abstract fun categoryDao(): CategoryDao?
    abstract fun recipeCategoryLinkDao(): RecipeCategoryLinkDao?
    abstract fun shoppingListDao(): ShoppingListDao?
    abstract fun shoppingListItemDao(): ShoppingListItemDao?

    companion object {
        //private var instance: AppDatabase? = null
        //private var appContext: Context? = null

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private var appContext: Context? = null

        fun getDatabase(context: Context): AppDatabase {
            appContext = context
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "i_do_the_cooking"
                ).addCallback(sRoomDatabaseCallback).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private val sRoomDatabaseCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                PopulateDbAsync(appContext).execute()
            }
        }

        private class PopulateDbAsync(appContext: Context?) :
            AsyncTask<Void?, Void?, Void?>() {
            private val mainApp: IDoTheCookingApp

            init {
                mainApp = appContext as IDoTheCookingApp
            }

//            private fun getStr(id: Int): String {
//                return appContext?.getResources()?.getString(id) ?: ""
//            }

            @Deprecated("Deprecated in Java")
            @OptIn(DelicateCoroutinesApi::class)
            override fun doInBackground(vararg p0: Void?): Void? {
                GlobalScope.future {
                    initDatabase()
                }
                return null
            }

            fun ReadJSONFromAssets(context: Context, path: String): String {
                val identifier = "[ReadJSON]"
                try {
                    val file = context.assets.open("$path")
                    val bufferedReader = BufferedReader(InputStreamReader(file))
                    val stringBuilder = StringBuilder()
                    bufferedReader.useLines { lines ->
                        lines.forEach {
                            stringBuilder.append(it)
                        }
                    }
                    val jsonString = stringBuilder.toString()
                    return jsonString
                } catch (e: Exception) {
                    e.printStackTrace()
                    return ""
                }
            }

            suspend fun initDatabase() {
                val json: String? = appContext?.let { ReadJSONFromAssets(it, "init_basic.json") }
                if (json != null) {
                    val p = JsonImportTool(mainApp, emptyMap<String, Int>().toMutableMap(),
                                   emptyMap<String, Int>().toMutableMap(),
                                   emptyMap<String, Int>().toMutableMap())
                    p.import(json)
                }
            }
        }
    }
}