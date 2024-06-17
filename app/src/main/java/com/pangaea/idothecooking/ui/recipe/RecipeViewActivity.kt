package com.pangaea.idothecooking.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.pangaea.idothecooking.utils.formatting.IngredientFormatter

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

            val detailsCanvas = binding.recipeDetailsCanvas
            val inputTitle = TextView(this)
            inputTitle.inputType = InputType.TYPE_CLASS_TEXT
            inputTitle.text = recipeDetails.recipe.name
            detailsCanvas.addView(inputTitle)
            val inputDesc = TextView(this)
            inputDesc.isElegantTextHeight = true;
            inputDesc.isSingleLine = false;
            //inputDesc.inputType = InputType.TYPE_CLASS_TEXT
            inputDesc.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            inputDesc.text = recipeDetails.recipe.description
            detailsCanvas.addView(inputDesc)

            val ingredientsCanvas = binding.recipeIngredientsCanvas
            recipeDetails.ingredients.forEach { ingredient: Ingredient ->
                val input = TextView(this)
                input.inputType = InputType.TYPE_CLASS_TEXT
                input.text = IngredientFormatter.formatDisplay(this, ingredient)
                ingredientsCanvas.addView(input)
            }

            val instructionsCanvas = binding.recipeInstructionsCanvas
            recipeDetails.directions.forEachIndexed { index, direction: Direction ->
                val input = TextView(this)
                //input.width = WindowManager.LayoutParams.MATCH_PARENT
                input.isElegantTextHeight = true;
                input.isSingleLine = false;
                input.setLines(2)
                input.minLines = 2
                input.maxLines = 10
                //input.inputType = InputType.TYPE_CLASS_TEXT
                input.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                //input.text = IngredientFormatter.formatDisplay(this, ingredient)
                input.text = (index+1).toString() + ") " + direction.content
                instructionsCanvas.addView(input)
            }
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
        return true
    }
}