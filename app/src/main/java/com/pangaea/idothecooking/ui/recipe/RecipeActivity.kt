package com.pangaea.idothecooking.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.ActivityRecipeBinding
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.adapters.RecipePagerAdapter
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.recipe.viewmodels.SelectedRecipeModel
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.ui.shared.adapters.CreateRecipeAdapter
import com.pangaea.idothecooking.utils.extensions.observeOnce
import com.pangaea.idothecooking.utils.extensions.setAsDisabled
import com.pangaea.idothecooking.utils.extensions.setAsEnabled
import com.pangaea.idothecooking.utils.extensions.startActivityWithBundle

class RecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeBinding
    private var recipeId: Int = -1
    private lateinit var sectionsPagerAdapter: RecipePagerAdapter
    private lateinit var viewModel: RecipeViewModel
    private var _itemSave: MenuItem? = null
    private val selectedRecipeModel: SelectedRecipeModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent?.extras
        var tab: String = ""
        if (bundle != null) {
            recipeId = bundle.getInt("id", -1)
            tab = bundle.getString("tab", "")
        }

        setSupportActionBar(binding.toolbar)

        viewModel = RecipeViewModelFactory(application, recipeId.toLong()).create(RecipeViewModel::class.java)
        viewModel.getDetails()?.observeOnce(this) { recipes ->
            val recipeDetails = recipes[0]
            selectedRecipeModel.setRecipeDetails(recipeDetails)
            title = resources.getString(R.string.title_activity_recipe_name)
                .replace("{0}", recipeDetails.recipe.name)
            sectionsPagerAdapter = RecipePagerAdapter(supportFragmentManager, 4, lifecycle)
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
                        3 -> resources.getString(R.string.suggestions_tab)
                        else -> resources.getString(R.string.overview_tab)
                    }
            }.attach()

            // Watch for changes
            var bLock: Boolean = true // Ignore the first event
            selectedRecipeModel.selectedRecipe.observe(this) { recipeDetails ->
                // Note: This is acting as a notification of any changes to the recipe
                //       so the save button can be enabled.
                if (!bLock) {
                    _itemSave?.setAsEnabled()
                } else {
                    bLock = false
                    if (tab == "suggestions") {
                        tabs.getTabAt(3)?.select()
                    }
                }
            }
        }

        // Handle back navigation
        //val self = this
        val callbackBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                this.remove()
                tryNavigateToViewActivity()
            }
        }
        onBackPressedDispatcher.addCallback(this, callbackBack)
    }

    fun tryNavigateToViewActivity() {
        if (_itemSave?.isEnabled == true) {
            selectedRecipeModel.selectedRecipe.observeOnce(this) { recipeDetails ->
                val saveChangesAlertBuilder = AlertDialog.Builder(this)
                    .setMessage(resources.getString(R.string.exit_with_save))
                    .setCancelable(true)
                    .setNegativeButton(resources.getString(R.string.no)) { dialog, _ -> navigateToViewActivity(recipeId) }
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        viewModel.update(recipeDetails) { navigateToViewActivity(recipeId) }
                    }
                val saveChangesAlert = saveChangesAlertBuilder.create()
                saveChangesAlert.show()
            }
        } else {
            navigateToViewActivity(recipeId)
        }
    }

    private fun navigateToViewActivity(id: Int) {
        startActivityWithBundle(RecipeViewActivity::class.java, "id", id)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.recipe_menu, menu)
        val itemCancel = menu.findItem(R.id.item_cancel)
        itemCancel.setOnMenuItemClickListener { menuItem ->
            tryNavigateToViewActivity()
            false
        }

        val itemSave = menu.findItem(R.id.item_save)
        _itemSave = itemSave
        _itemSave?.setAsDisabled()
        itemSave.setOnMenuItemClickListener {
            selectedRecipeModel.selectedRecipe.observeOnce(this) { recipeDetails ->
                viewModel.update(recipeDetails) {
                    Toast.makeText(applicationContext,
                                   getString(R.string.save_success),
                                   Toast.LENGTH_LONG).show()

                    // Reset item statuses
                    recipeDetails.ingredients.forEach(){o -> o.id = 1}
                    recipeDetails.directions.forEach(){o -> o.id = 1}
                    selectedRecipeModel.setRecipeDetails(recipeDetails)
                    _itemSave?.setAsDisabled()
                }
            }
            false
        }

        val itemSaveAs = menu.findItem(R.id.item_save_as)
        itemSaveAs.setOnMenuItemClickListener {
            selectedRecipeModel.selectedRecipe.observeOnce(this) { recipeDetails ->
                NameOnlyDialog(R.string.save_as_prompt, recipeDetails.recipe.name) { name ->
                    recipeDetails.recipe.id = 0
                    recipeDetails.recipe.name = name
                    CreateRecipeAdapter(this, this.baseContext,
                                        this, this.supportFragmentManager,
                                        viewModel) { id ->
                        // Reset item statuses and id
//                        recipeDetails.recipe.id = id.toInt()
//                        recipeDetails.ingredients.forEach(){o -> o.id = 1}
//                        recipeDetails.directions.forEach(){o -> o.id = 1}
//                        selectedRecipeModel.setRecipeDetails(recipeDetails)
//                        _itemSave?.setAsDisabled()
                        startActivityWithBundle(RecipeActivity::class.java, "id", id.toInt())
                        finish()
                    }.attemptRecipeInsert(recipeDetails)
                }.show(supportFragmentManager, null)
            }
            false
        }

        return true
    }
}
