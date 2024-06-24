package com.pangaea.idothecooking.state.db

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
import com.pangaea.idothecooking.state.db.dao.CategoryDao
import com.pangaea.idothecooking.state.db.dao.RecipeCategoryLinkDao
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future

@Database(version = 1, entities = [Recipe::class, Ingredient::class, Direction::class,
    Category::class, RecipeCategoryLink::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao?
    abstract fun recipeDirectionDao(): RecipeDirectionDao?
    abstract fun recipeIngredientDao(): RecipeIngredientDao?
    abstract fun categoryDao(): CategoryDao?
    abstract fun recipeCategoryLinkDao(): RecipeCategoryLinkDao?

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

        fun createIngredient(order: Int, count: Double, unit: String, name: String): Ingredient {
            val ingredient = Ingredient()
            ingredient.order = order
            ingredient.amount = count
            ingredient.unit = unit
            ingredient.name = name
            return ingredient
        }

        fun createDirection(order: Int, content: String): Direction {
            val direction = Direction()
            direction.order = order
            direction.content = content
            return direction
        }

        private class PopulateDbAsync(appContext: Context?) :
            AsyncTask<Void?, Void?, Void?>() {
            private val mainApp: IDoTheCookingApp

            init {
                mainApp = appContext as IDoTheCookingApp
            }

            private fun getStr(id: Int): String {
                return appContext?.getResources()?.getString(id) ?: ""
            }

            @Deprecated("Deprecated in Java")
            @OptIn(DelicateCoroutinesApi::class)
            override fun doInBackground(vararg p0: Void?): Void? {
                GlobalScope.future {
                    initDatabase()
                }
                return null
            }

            suspend fun initDatabase() {
                val db: AppDatabase = mainApp.getDatabase()

                val categoryDao : CategoryDao? = db.categoryDao()
                val categoryRepository : CategoryRepository? = categoryDao?.let {
                    CategoryRepository(it)
                }
                val cat1 = Category(0, "Breakfast")
                var cat1Id: Long = 0; var cat2Id: Long = 0; var cat3Id: Long = 0; var cat4Id: Long = 0
                categoryRepository?.insert(cat1) {cat1Id = it}
                val cat2 = Category(0, "Lunch")
                categoryRepository?.insert(cat2) {cat2Id = it}
                val cat3 = Category(0, "Dinner")
                categoryRepository?.insert(cat3) {cat3Id = it}
                val cat4 = Category(0, "Desert")
                categoryRepository?.insert(cat4) {cat4Id = it}


                val recipeRepo = db.recipeDao()?.let {
                    db.recipeDirectionDao()
                        ?.let { it1 ->
                            db.recipeIngredientDao()
                                ?.let { it2 -> db.recipeCategoryLinkDao()
                                    ?.let { it3 -> RecipeRepository(it, it1, it2, it3) } }
                        }
                }

                val recipe = Recipe()
                recipe.name = "Grilled Cheese"
                recipe.description = "Made with two different kinds of cheese"
                val directions: MutableList<Direction> = arrayListOf()
                directions.add(
                    createDirection(
                        0,
                        "Put both american and cheddar cheese in bread"
                    )
                )
                directions.add(createDirection(1, "Heat butter in a pan"))
                directions.add(
                    createDirection(
                        2,
                        "Cook sandwich in pan until cheese is melted and the bread is nice a brown"
                    )
                )
                val ingredients: MutableList<Ingredient> = arrayListOf()
                ingredients.add(Ingredient(0, 0, 0, "Potato bread",2.00, ""))
                ingredients.add(createIngredient(1, 3.00, "", "American cheese"))
                ingredients.add(createIngredient(2, 2.50, "tbsp", "Butter"))
                val categories: MutableList<RecipeCategoryLink> = arrayListOf()
                categories.add(RecipeCategoryLink(0, 0, cat1Id.toInt()))
                categories.add(RecipeCategoryLink(0, 0, cat2Id.toInt()))
                val details = RecipeDetails(recipe, ingredients, directions, categories)
                recipeRepo?.insert(details) {}


                val recipe2 = Recipe()
                recipe2.name = "Hot Dog"
                recipe2.description = "Boiled in water"
                val directions2: MutableList<Direction> = arrayListOf()
                directions2.add(createDirection(0, "Boil water"))
                directions2.add(createDirection(1, "Put hot dogs in boiling water"))
                val ingredients2: MutableList<Ingredient> = arrayListOf()
                ingredients2.add(createIngredient(0, 1.00, "", "Hot dog"))
                ingredients2.add(createIngredient(0, 1.00, "", "Hot dog bun"))
                val categories2: MutableList<RecipeCategoryLink> = arrayListOf()
                categories2.add(RecipeCategoryLink(0, 0, cat3Id.toInt()))
                categories2.add(RecipeCategoryLink(0, 0, cat4Id.toInt()))
                val details2 = RecipeDetails(recipe2, ingredients2, directions2, categories2)
                recipeRepo?.insert(details2) {}
            }
        }
    }
}