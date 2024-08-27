package com.pangaea.idothecooking.utils.data

import android.app.Application
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.pangaea.idothecooking.state.CategoryRepository
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.ShoppingListRepository
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem

class JsonImportTool(val app: Application, private var categoryMap: MutableMap<String, Int>,
                     private var recipeMap: MutableMap<String, Int>,
                     private var shoppingListMap: MutableMap<String, Int>) {
    enum class MessageType { ERROR, WARNING, INFORMATION }
    data class ParseLog(val type: MessageType, val message: String)

    private val categoryRepository = CategoryRepository(app)
    private val recipeRepo = RecipeRepository(app)
    private val shoppingListRepo = ShoppingListRepository(app)
    private val messages: MutableList<ParseLog> = emptyList<ParseLog>().toMutableList()

    suspend fun import(json: String): List<ParseLog> {
        val mapper = ObjectMapper()
        val node: JsonNode = mapper.readTree(json)

        val categoriesNode: JsonNode? = node.get("categories")
        if (categoriesNode != null && categoriesNode.isArray) {
            // Build a list of categories not already in the db
            val newCategories: MutableList<Category> = emptyList<Category>().toMutableList()
            for (objNode in categoriesNode) {
                if (categoryMap[objNode.get("name").textValue()] == null) {
                    newCategories.add(mapper.convertValue(objNode, Category::class.java))
                }
            }

            if (newCategories.isNotEmpty()) {
                // Bulk insert new categories and add them to map of ids
                val ids = categoryRepository.bulkInsert(newCategories)
                newCategories.forEachIndexed { index, category ->
                    categoryMap[category.name] = ids[index].toInt()
                }
            }
        }
        importRecipes(node)
        importShoppingLists(node)
        messages.add(ParseLog(MessageType.INFORMATION, "Import complete"))
        return messages;
    }

    private suspend fun importRecipes(node: JsonNode) {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        val recipesNode: JsonNode? = node.get("recipes")
        if (recipesNode != null && recipesNode.isArray) {
            val newRecipes: MutableList<RecipeDetails> = emptyList<RecipeDetails>().toMutableList()
            for (objNode in recipesNode) {
                if (recipeMap[objNode.get("name").textValue()] == null) {
                    newRecipes.add(RecipeDetails(// Recipe
                        mapper.convertValue(objNode, Recipe::class.java),
                        // List<Ingredients>
                        objNode.get("ingredients").map{
                            mapper.convertValue(it, Ingredient::class.java)
                        },
                        // List<Directions>
                        objNode.get("directions").map{
                            mapper.convertValue(it, Direction::class.java)
                        },
                        // List<RecipeCategoryLink>
                        objNode.get("categories").filter{
                            categoryMap[it.asText()] != null}.map{
                            val link = RecipeCategoryLink()
                            link.category_id = categoryMap[it.asText()]!!
                            link
                        }))
                }
            }

            newRecipes.forEach {recipe ->
                try {
                    val id = recipeRepo.insert(recipe)
                    recipeMap[recipe.recipe.name] = id.toInt()
                } catch (e: Exception) {
                    messages.add(ParseLog(MessageType.ERROR, e.message.let{e.message} ?:
                        ("Error saving recipe " + recipe.recipe.name)))
                }
            }
            println("Recipe Import Complete")
        }
    }

    private suspend fun importShoppingLists(node: JsonNode) {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        val shoppingListsNode: JsonNode? = node.get("shoppingLists")
        if (shoppingListsNode != null  && shoppingListsNode.isArray) {
            val newShoppingLists: MutableList<ShoppingListDetails> = emptyList<ShoppingListDetails>().toMutableList()
            for (objNode in shoppingListsNode) {
                if (shoppingListMap[objNode.get("name").textValue()] == null) {
                    newShoppingLists.add(ShoppingListDetails(// ShoppingList
                        mapper.convertValue(objNode, ShoppingList::class.java),
                        // List<ShoppingListItems>
                        objNode.get("shoppingListItems").map{
                            mapper.convertValue(it, ShoppingListItem::class.java)
                        }))
                }
            }

            newShoppingLists.forEach {shoppingList ->
                try {
                    val id = shoppingListRepo.insert(shoppingList)
                    recipeMap[shoppingList.shoppingList.name] = id.toInt()
                } catch (e: Exception) {
                    messages.add(ParseLog(MessageType.ERROR, e.message.let{e.message} ?:
                        ("Error saving shopping list " + shoppingList.shoppingList.name)))
                }
            }
            println("Shopping List Import Complete")
        }
    }
}
