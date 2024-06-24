package com.pangaea.idothecooking.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.ActivityRecipeViewBinding
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.utils.extensions.vulgarFraction


class RecipeViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeViewBinding
    private lateinit var viewModel: RecipeViewModel
    private var recipeId: Int = -1
    private lateinit var recipeDetails: RecipeDetails
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecipeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setBackgroundResource(R.mipmap.tablecloth3)

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

            val ingredientBuilder = StringBuilder()
            val htmlRecipeIngredient = getString(com.pangaea.idothecooking.R.string.html_recipe_ingredient)
            recipeDetails.ingredients.forEach { ingredient: Ingredient ->
                val frac: Pair<String, Double>? = ingredient.amount?.vulgarFraction
                if (frac != null) {
                    val amount = frac.first + " " + ingredient.unit
                    ingredientBuilder.append(htmlRecipeIngredient.replace("{{0}}", amount)
                                                 .replace("{{1}}", ingredient.name))

                } else {
                    ingredientBuilder.append(htmlRecipeIngredient.replace("{{0}}", "")
                                                 .replace("{{1}}", ingredient.name))
                }
            }

            val directionBuilder = StringBuilder()
            val htmlRecipeDirection = getString(com.pangaea.idothecooking.R.string.html_recipe_direction)
            recipeDetails.directions.forEachIndexed { index, direction: Direction ->
                directionBuilder.append(htmlRecipeDirection.replace("{{0}}", direction.content)
                                            .replace("{{1}}", (index+1).toString()))
            }

            var imageElem = ""
            if (!recipeDetails.recipe.imageUri.isNullOrEmpty()) {
                imageElem = "<img length=\"100px\" width=\"100px\" src=\""+ recipeDetails.recipe.imageUri.toString() + "\">";
            }

            var htmlRecipe = getString(com.pangaea.idothecooking.R.string.html_recipe)
            htmlRecipe = htmlRecipe.replace("{{0}}", recipeDetails.recipe.name).replace("{{1}}", recipeDetails.recipe.description)
                .replace("{{2}}", ingredientBuilder.toString())
                .replace("{{3}}", directionBuilder.toString())
                .replace("{{image}}", imageElem)

            if (recipeDetails.recipe.servings > 0) {
                val servingsDisplay = getString(com.pangaea.idothecooking.R.string.servings_display)
                htmlRecipe = htmlRecipe.replace("{{4}}", servingsDisplay.replace("{{0}}", recipeDetails.recipe.servings.toString()))
            } else {
                htmlRecipe = htmlRecipe.replace("{{4}}", "")
            }
            binding.viewport.loadDataWithBaseURL(null, htmlRecipe, "text/html", "utf-8", null);
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.recipe_view_menu, menu)
        val itemCancel = menu.findItem(R.id.item_cancel)
        itemCancel.setOnMenuItemClickListener { menuItem ->
            onBackPressed()
            false
        }
        val itemEdit = menu.findItem(R.id.item_edit)
        itemEdit.setOnMenuItemClickListener { menuItem ->
            val intent = Intent(this, RecipeActivity::class.java)
            val b = Bundle()
            b.putInt("id", recipeId)
            intent.putExtras(b)
            startActivity(intent)
            false
        }
        return true
    }
}