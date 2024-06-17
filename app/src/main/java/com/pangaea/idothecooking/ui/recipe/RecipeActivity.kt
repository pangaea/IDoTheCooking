package com.pangaea.idothecooking.ui.recipe

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.ActivityRecipeBinding
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.adapters.RecipePagerAdapter
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.utils.ThrottledUpdater

class RecipeActivity : AppCompatActivity(), RecipeCallBackListener {

    private lateinit var binding: ActivityRecipeBinding
    private var recipeId: Int = -1
    private lateinit var sectionsPagerAdapter: RecipePagerAdapter
    private lateinit var viewModel: RecipeViewModel
    private lateinit var recipeDetails: RecipeDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent?.extras
        if (bundle != null) {
            recipeId = bundle.getInt("id", -1)
        }

        setSupportActionBar(binding.toolbar)
        val db: AppDatabase = (application as IDoTheCookingApp).getDatabase()
        val recipeRepo = db.recipeDao()?.let {
            db.recipeDirectionDao()
                ?.let { it1 ->
                    db.recipeIngredientDao()?.let { it2 -> db.recipeCategoryLinkDao()
                        ?.let { it3 -> RecipeRepository(it, it1, it2, it3) } }
                }
        }

        viewModel = recipeRepo?.let {
            RecipeViewModelFactory(it, recipeId.toLong())
                .create(RecipeViewModel::class.java)
        }!!

        viewModel.getDetails()?.observe(this) { recipes ->
            recipeDetails = recipes[0]
            title = resources.getString(R.string.title_activity_recipe_name)
                .replace("{0}", recipeDetails.recipe.name)
            sectionsPagerAdapter = RecipePagerAdapter(supportFragmentManager, 3, lifecycle, recipeDetails)
            val viewPager: ViewPager2 = binding.viewPager
            binding.viewPager.isUserInputEnabled = false
            viewPager.adapter = sectionsPagerAdapter
            val tabs: TabLayout = binding.tabs
            //tabs.set.setupWithViewPager(viewPager)
            TabLayoutMediator(tabs, viewPager) { tab, position ->
                tab.text =
                    when (position) {
                        0 -> resources.getString(R.string.overview_tab)
                        1 -> resources.getString(R.string.ingredients_tab)
                        2 -> resources.getString(R.string.instructions_tab)
                        else -> resources.getString(R.string.overview_tab)
                    }
            }.attach()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.recipe_menu, menu)
        val itemCancel = menu.findItem(R.id.item_cancel)
        itemCancel.setOnMenuItemClickListener { menuItem ->
            if (dataDirty) {
                val deleteAlertBuilder = AlertDialog.Builder(this)
                deleteAlertBuilder.setMessage(resources.getString(R.string.exit_without_save))
                deleteAlertBuilder.setCancelable(true)
                deleteAlertBuilder.setPositiveButton(resources.getString(R.string.yes)) { _, _ -> onBackPressed() }
                deleteAlertBuilder.setNegativeButton(resources.getString(R.string.no)) { dialog, _ -> dialog.cancel() }
                val deleteAlert = deleteAlertBuilder.create()
                deleteAlert.show()
            } else {
                onBackPressed()
            }
            false
        }

        val itemPrint = menu.findItem(R.id.item_print)
        itemPrint.setOnMenuItemClickListener { menuItem ->
            val intent = Intent(this, RecipeViewActivity::class.java)
            val b = Bundle()
            b.putInt("id", recipeId)
            intent.putExtras(b)
            startActivity(intent)
            false
        }

        val itemSave = menu.findItem(R.id.item_save)
        itemSave.setOnMenuItemClickListener { menuItem ->
            viewModel.update(recipeDetails){}
//            viewModel.update(recipeDetails) {
//                onBackPressed()
//            }
            onBackPressed()
            false
        }
        return true
    }

    override fun getRecipeDetails(): RecipeDetails {
        return recipeDetails;
    }

    var dataDirty: Boolean = false
    private val infoUpdater = ThrottledUpdater()
    private val ingredientUpdater = ThrottledUpdater()
    private val directionsUpdater = ThrottledUpdater()

    override fun onRecipeInfoUpdate(recipe: Recipe) {
        infoUpdater.delayedUpdate(){
            Log.d(TAG, "onRecipeInfoUpdate")
            // TODO: Find a better way to do this
            dataDirty = true
            recipeDetails.recipe.name = recipe.name
            recipeDetails.recipe.description = recipe.description
            recipeDetails.recipe.imageUri = recipe.imageUri
            recipeDetails.recipe.servings = recipe.servings
        }
    }

    override fun onRecipeDirectionUpdate(directions: List<Direction>) {
        directionsUpdater.delayedUpdate(){
            Log.d(TAG, "onRecipeDirectionUpdate")
            dataDirty = true
            recipeDetails.directions = directions
        }
    }

    override fun onRecipeIngredientUpdate(ingredients: List<Ingredient>) {
        ingredientUpdater.delayedUpdate(){
            Log.d(TAG, "onRecipeIngredientUpdate")
            dataDirty = true
            recipeDetails.ingredients = ingredients
        }
    }

    override fun onRecipeCategories(categories: List<Category>) {
        infoUpdater.delayedUpdate(){
            Log.d(TAG, "onRecipeCategories")
            dataDirty = true
            recipeDetails.categories = categories.map { o ->
                RecipeCategoryLink(0, recipeId, o.id)
            }
        }
    }
}
