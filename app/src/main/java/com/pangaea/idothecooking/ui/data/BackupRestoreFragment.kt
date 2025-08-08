package com.pangaea.idothecooking.ui.data

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentBackupRestoreBinding
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.data.JsonAsyncImportTool
import com.pangaea.idothecooking.utils.data.JsonImportTool.MessageType
import com.pangaea.idothecooking.utils.extensions.observeOnce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date

class BackupRestoreFragment : Fragment() {
    val OPEN_DOCUMENT_REQUEST_CODE = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBackupRestoreBinding.inflate(inflater, container, false)

        binding.backupData.setOnClickListener(){
            generateRecipeExport()
        }

        binding.restoreData.setOnClickListener(){
            val openDocumentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(openDocumentIntent, OPEN_DOCUMENT_REQUEST_CODE)
        }

        return binding.root
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
                        val stream: InputStream? = requireActivity().application.contentResolver.openInputStream(contentUri)
                        if (stream != null) {
                            JsonAsyncImportTool(requireActivity().application, this).loadData() { tool, ctx ->
                                tool.import(stream.readBytes().toString(Charset.defaultCharset()), null, ctx) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if (it.isNotEmpty() && it[it.size-1].type == MessageType.ERROR) {
                                            Toast.makeText(requireActivity().applicationContext, it.get(0).message, Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(requireActivity().applicationContext,
                                                           getString(R.string.import_complete),
                                                           Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        }
                    } catch (exception: Exception) {
                        Toast.makeText(requireActivity().applicationContext, exception.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun generateRecipeExport() {
        val mapper = ObjectMapper()

        val categoryViewModel = CategoryViewModelFactory(requireActivity().application, null).create(CategoryViewModel::class.java)
        categoryViewModel.getAllCategories().observeOnce(this) { categories ->

            val rootNode: ObjectNode = mapper.createObjectNode()
            rootNode.put("categories", mapper.convertValue(categories, JsonNode::class.java))
            val categoryMap = categories.associateBy({it.id}, {it.name})

            val recipeViewModel = RecipeViewModelFactory(requireActivity().application, null).create(RecipeViewModel::class.java)
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

                val shoppingListViewModel = ShoppingListViewModelFactory((requireActivity().application),null).create(
                    ShoppingListViewModel::class.java)
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
                        type = "application/json"
                    }
                    startActivity(sendIntent)

                    // TODO: Since we don't know if the user actually saves the file we set backup time here
                    val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val pref = sharedPreferences.edit()
                    pref.putLong("last_backup_time", System.currentTimeMillis())
                    pref.apply()
                }
            }
        }
    }
}