package com.pangaea.idothecooking.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import com.pangaea.idothecooking.MainActivity
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.ActivityRecipeViewBinding
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shared.NumberOnlyDialog
import com.pangaea.idothecooking.ui.shared.ShareAndPrintActivity
import com.pangaea.idothecooking.ui.shared.PicklistDlg
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.data.IngredientsMigrationTool
import com.pangaea.idothecooking.utils.extensions.observeOnce
import com.pangaea.idothecooking.utils.extensions.startActivityWithBundle
import com.pangaea.idothecooking.utils.formatting.RecipeRenderer

class RecipeViewActivity : ShareAndPrintActivity() {
    private lateinit var binding: ActivityRecipeViewBinding
    private lateinit var viewModel: RecipeViewModel
    private var recipeId: Int = -1
    private lateinit var recipeDetails: RecipeDetails
    private var servingSize: Int = 0
    private val categoryMap = emptyMap<Int, String>().toMutableMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecipeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding.root.setBackgroundResource(R.mipmap.tablecloth3)

        val bundle = intent?.extras
        if (bundle != null) {
            recipeId = bundle.getInt("id", -1)
        }

        setSupportActionBar(binding.toolbar)

        viewModel = RecipeViewModelFactory(application, recipeId.toLong()).create(RecipeViewModel::class.java)
        val categoryViewModel = CategoryViewModelFactory(application, null).create(CategoryViewModel::class.java)

        viewModel.getDetails()?.observe(this) { recipes ->
            recipeDetails = recipes[0]
            servingSize = recipeDetails.recipe.servings
            title = resources.getString(R.string.title_activity_recipe_name).replace("{0}", recipeDetails.recipe.name)
            categoryViewModel.getAllCategories().observe(this) { categories ->
                categoryMap.putAll(categories.associateBy({ it.id }, { it.name }).toMap())
                drawRecipe()
            }
        }

        binding.materialSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        val callbackBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this.remove()
                navigateToRecipeList()
            }
        }
        onBackPressedDispatcher.addCallback(this, callbackBack)

        binding.enhanceButton.setOnClickListener {
            //startActivityWithBundle(RecipeActivity::class.java, "id", recipeId)
            val aiBundle = Bundle()
            aiBundle.putInt("id", recipeId)
            aiBundle.putString("tab", "suggestions")
            val intent = Intent(this, RecipeActivity::class.java)
            intent.putExtras(aiBundle)
            startActivity(intent)
        }
    }

    fun navigateToRecipeList() {
        startActivityWithBundle(MainActivity::class.java, "start", "recipes")
    }

    private fun drawRecipe() {
        val htmlRecipe = RecipeRenderer(this.applicationContext, recipeDetails, servingSize, categoryMap).drawRecipeHtml()
        binding.viewport.loadDataWithBaseURL(null, htmlRecipe, "text/html", "utf-8", null);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.recipe_view_menu, menu)

        // Close
        val itemCancel = menu.findItem(R.id.item_cancel)
        itemCancel.setOnMenuItemClickListener { menuItem ->
            //onBackPressed()
            navigateToRecipeList()
            false
        }

        // Edit recipe
        val itemEdit = menu.findItem(R.id.item_edit)
        itemEdit.setOnMenuItemClickListener { menuItem ->
            startActivityWithBundle(RecipeActivity::class.java, "id", recipeId)
            false
        }

        // Share recipe via SMS
        val itemShare = menu.findItem(R.id.item_share)
        itemShare.setOnMenuItemClickListener { menuItem ->
            val textRecipe = RecipeRenderer(this.applicationContext, recipeDetails, servingSize, categoryMap).drawRecipeText()
            sendMessage(recipeDetails.recipe.name, textRecipe)
            false
        }

        // Print recipe
        val itemPrint = menu.findItem(R.id.item_print)
        itemPrint.setOnMenuItemClickListener { menuItem ->
            createWebPrintJob(recipeDetails.recipe.name, binding.viewport)
            false
        }

        // Adjust serving size
        val itemServSize = menu.findItem(R.id.item_serv_size)
        itemServSize.setOnMenuItemClickListener { menuItem ->
            if (servingSize == 0){
                Toast.makeText(baseContext, getString(R.string.adjust_servings_error), Toast.LENGTH_LONG).show()
            } else {
                NumberOnlyDialog(R.string.adjust_servings_title, servingSize) {
                    servingSize = it
                    drawRecipe()
                }.show(this.supportFragmentManager, null)
            }
            false
        }

        // Export recipe ingredients
        val exportToList = menu.findItem(R.id.export_to_list)
        exportToList.setOnMenuItemClickListener { menuItem ->
            val model = ShoppingListViewModelFactory(application, null).create(ShoppingListViewModel::class.java)
            model.getAllShoppingLists().observeOnce(this) { shoppingLists ->
                val options = listOf<Pair<String, String>>().toMutableList()
                options.add(Pair<String, String>("0", getString(R.string.category_create_new)))
                options.addAll(shoppingLists.map() { o -> Pair(o.id.toString(), o.name) }.toMutableList())
                PicklistDlg(getString(R.string.export_to_shopping_list),
                            options) { shoppingList: Pair<String, String> ->
                    var adjRatio = 1.0
                    if (recipeDetails.recipe.servings > 0) {
                        // Avoid division by zero
                        adjRatio = (servingSize.toDouble() / recipeDetails.recipe.servings)
                    }
                    IngredientsMigrationTool(application, this, recipeDetails.recipe.id, recipeDetails.recipe.name, adjRatio,
                                             shoppingList.first.toInt()).execute() {
                        Toast.makeText(baseContext, getString(R.string.success_export_to_shopping_list), Toast.LENGTH_LONG).show()
                    }
                }.show(this.supportFragmentManager, null)
            }
            false
        }

        return true
    }
}