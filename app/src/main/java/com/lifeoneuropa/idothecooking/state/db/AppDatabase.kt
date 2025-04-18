package com.lifeoneuropa.idothecooking.state.db

import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lifeoneuropa.idothecooking.IDoTheCookingApp
import com.lifeoneuropa.idothecooking.state.db.dao.RecipeDao
import com.lifeoneuropa.idothecooking.state.db.dao.RecipeDirectionDao
import com.lifeoneuropa.idothecooking.state.db.dao.RecipeIngredientDao
import com.lifeoneuropa.idothecooking.state.db.entities.Direction
import com.lifeoneuropa.idothecooking.state.db.entities.Ingredient
import com.lifeoneuropa.idothecooking.state.db.entities.Recipe
import com.lifeoneuropa.idothecooking.state.db.dao.CategoryDao
import com.lifeoneuropa.idothecooking.state.db.dao.RecipeCategoryLinkDao
import com.lifeoneuropa.idothecooking.state.db.dao.ShoppingListDao
import com.lifeoneuropa.idothecooking.state.db.dao.ShoppingListItemDao
import com.lifeoneuropa.idothecooking.state.db.entities.Category
import com.lifeoneuropa.idothecooking.state.db.entities.RecipeCategoryLink
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingList
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingListItem
import com.lifeoneuropa.idothecooking.utils.data.JsonImportTool
import com.lifeoneuropa.idothecooking.utils.extensions.readContentFromAssets
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future

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

            suspend fun initDatabase() {
                val json: String? = appContext?.readContentFromAssets("init_basic.json")
                if (json != null) {
                    val p = JsonImportTool(mainApp, null, emptyMap<String, Int>().toMutableMap(),
                                   emptyMap<String, Int>().toMutableMap(),
                                   emptyMap<String, Int>().toMutableMap())
                    p.import(json)
                }
            }
        }
    }
}