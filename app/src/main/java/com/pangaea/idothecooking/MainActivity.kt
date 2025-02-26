package com.pangaea.idothecooking

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.android.material.navigation.NavigationView
import com.pangaea.idothecooking.databinding.ActivityMainBinding
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.settings.SettingsActivity
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.data.JsonAsyncImportTool
import com.pangaea.idothecooking.utils.extensions.observeOnce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_recipes, R.id.nav_shopping_lists
//            ), drawerLayout
//        )
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Navigate to fragment
        val bundle = intent?.extras
        if (bundle != null) {
            when (bundle.getString("start", null)) {
                "recipes" -> {
                    navController.navigate(R.id.nav_recipes)
                }
                "shoppingLists" -> {
                    navController.navigate(R.id.nav_shopping_lists)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    val OPEN_DOCUMENT_REQUEST_CODE = 2

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_export -> {
                generateRecipeExport()
                true
            }
            R.id.action_import -> {
                val openDocumentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                }
                startActivityForResult(openDocumentIntent, OPEN_DOCUMENT_REQUEST_CODE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //tryHandleOpenDocumentResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val contentUri = data.data
                if (contentUri != null) {
                    try {
                        val stream: InputStream? = application.contentResolver.openInputStream(contentUri)
                        if (stream != null) {
                            JsonAsyncImportTool(application, this).loadData() { tool, ctx ->
                                tool.import(stream.readAllBytes()
                                                .toString(Charset.defaultCharset()), null, ctx) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(applicationContext,
                                                       getString(R.string.import_complete),
                                                       Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    } catch (exception: FileNotFoundException) {
                        Toast.makeText(applicationContext, exception.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun generateRecipeExport() {
        val mapper = ObjectMapper()

        val categoryViewModel = CategoryViewModelFactory(application, null).create(CategoryViewModel::class.java)
        categoryViewModel.getAllCategories().observeOnce(this) { categories ->

            val rootNode: ObjectNode = mapper.createObjectNode()
            rootNode.put("categories", mapper.convertValue(categories, JsonNode::class.java))
            val categoryMap = categories.associateBy({it.id}, {it.name})

            val recipeViewModel = RecipeViewModelFactory(application, null).create(RecipeViewModel::class.java)
            recipeViewModel.getAllRecipesWithDetails().observeOnce(this) { recipes ->
                val recipeArray: ArrayNode = mapper.createArrayNode()
                recipes.forEach { recipeDetails ->
                    val jsonNode: JsonNode = mapper.convertValue(recipeDetails.recipe, JsonNode::class.java)
                    (jsonNode as ObjectNode).put("ingredients", mapper.convertValue(
                        recipeDetails.ingredients.sortedBy { it.order }, JsonNode::class.java))
                    jsonNode.put("directions", mapper.convertValue(
                        recipeDetails.directions.sortedBy { it.order }, JsonNode::class.java))
                    jsonNode.put("categories", mapper.convertValue(recipeDetails.categories.map { categoryMap[it.category_id] },
                                                                   JsonNode::class.java))
                    recipeArray.add(jsonNode)
                }
                rootNode.put("recipes", recipeArray)

                val shoppingListViewModel = ShoppingListViewModelFactory((application),null).create(ShoppingListViewModel::class.java)
                shoppingListViewModel.getAllShoppingListsWithDetails().observeOnce(this) { shoppingLists ->
                    val shoppingListArray: ArrayNode = mapper.createArrayNode()
                    shoppingLists.forEach { shoppingListDetails ->
                        val jsonNode: JsonNode = mapper.convertValue(shoppingListDetails.shoppingList, JsonNode::class.java)
                        (jsonNode as ObjectNode).put("shoppingListItems", mapper.convertValue(
                            shoppingListDetails.shoppingListItems.sortedBy { it.order },
                            JsonNode::class.java))
                        shoppingListArray.add(jsonNode)
                    }
                    rootNode.put("shoppingLists", shoppingListArray)

                    // Send backup data to Google drive
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, rootNode.toString())
                        val sdf = SimpleDateFormat("yyyy-M-dd hh:mm:ss")
                        val currentDate = sdf.format(Date())
                        putExtra(Intent.EXTRA_SUBJECT,
                                 resources.getString(R.string.app_internal_name) + "-" + currentDate + ".json")
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setPackage("com.google.android.apps.docs")
                        type = "text/json"
                    }
                    startActivity(sendIntent)
                }
            }
        }
    }
}