package com.pangaea.idothecooking.utils.connect

import android.content.Context
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.adapters.HelperSuggestion
import com.pangaea.idothecooking.utils.extensions.readContentFromAssets
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class LlmGateway(val context: Context) {
    private val openAiChatCompletionUrl = "https://api.openai.com/v1/chat/completions"
    private val mediaTypeJson: MediaType = "application/json".toMediaType()
    private val mockRequest = false

    fun suggestRecipe(desc: String, callback: (recipes: List<RecipeDetails>) -> Unit) {

        if (!mockRequest) {
            val promptSuggestRecipe = context.getString(R.string.prompt_suggest_recipes)
            llmRequest(promptSuggestRecipe.replace("{description}", desc)) {
                callback(parseRecipeListJson(it))
            }
        } else {
            // Mock data - for dev
            Thread.sleep(5_000)
            val data: String = context.readContentFromAssets("sample_openai_recipe_list.data")
            callback(parseRecipeListJson(data))
        }
    }

    fun suggestEnhancements(desc: String, recipe: RecipeDetails, callback: (recipes: List<HelperSuggestion>) -> Unit) {
        if (!mockRequest) {
            val promptSuggestEnhancements = context.getString(R.string.prompt_suggest_recipe_improvements)
            llmRequest(promptSuggestEnhancements.replace("{recipe_name}", recipe.recipe.name)
                           .replace("{ingredient_list}", recipe.ingredients.map{it.name}.joinToString(","))
                           .replace("{requested_improvements}", desc)) {
                callback(parseSuggestionListJson(it))
            }
        } else {
            // Mock data - for dev
            Thread.sleep(5_000)
            val data: String = context.readContentFromAssets("sample_openai_suggestions.data")
            callback(parseSuggestionListJson(data))
        }
    }

    private fun llmRequest(content: String, callback: (payload: String) -> Unit) {
        val jsonBody = JSONObject()
        try {
            jsonBody.put("model", "gpt-4o-mini")
            val jsonMsg = JSONObject()
            jsonMsg.put("role", "user")
            jsonMsg.put("content", content)
            val jsonMsgs = JSONArray()
            jsonMsgs.put(jsonMsg)
            jsonBody.put("messages", jsonMsgs)
            jsonBody.put("max_tokens", 4000)
            jsonBody.put("temperature", 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val openAiApiKey = com.pangaea.idothecooking.BuildConfig.OPENAI_API_KEY
        val body: RequestBody = RequestBody.create(mediaTypeJson, jsonBody.toString())
        val request: Request = Request.Builder()
            .url(openAiChatCompletionUrl)
            .header("Authorization", "Bearer $openAiApiKey")
            .post(body).build()
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Set connection timeout to 30 seconds
            .readTimeout(60, TimeUnit.SECONDS)    // Set read timeout to 30 seconds
            .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout to 30 seconds
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = response.body!!.string()
            val jsonRoot = JsonParser.parseString(json).asJsonObject;
            val choices = jsonRoot.get("choices").asJsonArray
            val choice = choices[0].asJsonObject
            val msg = choice.get("message").asJsonObject
            callback(msg.get("content").asString)
        }
    }

    /////////////////////////////////////////////////////////
    // Recipes
    ////////////////////////////////////////////////////////

    private fun parseRecipeListJson(data: String): List<RecipeDetails> {
        val recipeJson = extractJsonString(data)
        val mapper = ObjectMapper()
        val node: JsonNode = mapper.readTree(recipeJson)
        val recipes: MutableList<RecipeDetails> = emptyList<RecipeDetails>().toMutableList()
        val recipesNode: JsonNode? = node.get("recipes")
        extractRecipes(recipesNode ?: node, recipes)
        return recipes
    }

    private fun extractRecipes(node: JsonNode, recipes: MutableList<RecipeDetails>) {
        if (node.isArray) {
            node.forEachIndexed { _, objNode ->
                recipes.add(extractRecipeFromJson(objNode))
            }
        } else {
            recipes.add(extractRecipeFromJson(node))
        }
    }

//    private fun parseRecipeJson(data: String): RecipeDetails {
//        val recipeJson = extractJsonString(data)
//        val mapper = ObjectMapper()
//        val node: JsonNode = mapper.readTree(recipeJson)
//        val recipeNode: JsonNode? = node.get("recipe")
//        return if (recipeNode != null) {
//            extractRecipeFromJson(recipeNode)
//        } else {
//            extractRecipeFromJson(node)
//        }
//    }

    private fun extractRecipeFromJson(recipeNode: JsonNode?): RecipeDetails {
        val recipe = Recipe()
        recipe.name = recipeNode?.get("name")?.asText() ?: context.getString(R.string.new_ai_recipe)
        recipe.description = context.getString(R.string.generated_from_openai)

        val ingredients: MutableList<Ingredient> = emptyList<Ingredient>().toMutableList()
        val ingredientsNode: JsonNode? = recipeNode?.get("ingredients")
        if (ingredientsNode != null && ingredientsNode.isArray) {
            ingredientsNode.forEachIndexed { index, objNode ->
                val i = Ingredient()
                i.name = objNode.get("name").asText()
                i.amount = objNode.get("quantity").asDouble()
                i.unit = objNode.get("unit").asText()
                i.order = index
                ingredients.add(i)
            }
        }

        val directions: MutableList<Direction> = emptyList<Direction>().toMutableList()
        val directionsNode: JsonNode? = recipeNode?.get("directions")
        if (directionsNode != null && directionsNode.isArray) {
            directionsNode.forEachIndexed { index, objNode ->
                val d = Direction()
                d.content = objNode.asText()
                d.order = index
                directions.add(d)
            }
        }

        //println(recipeJson)
        return RecipeDetails(recipe, ingredients, directions, emptyList());
    }

    /////////////////////////////////////////////////////////
    // Suggestions
    ////////////////////////////////////////////////////////

    private fun parseSuggestionListJson(data: String): List<HelperSuggestion> {
        val recipeJson = extractJsonString(data)
        val mapper = ObjectMapper()
        val node: JsonNode = mapper.readTree(recipeJson)

        val suggestions: MutableList<HelperSuggestion> = emptyList<HelperSuggestion>().toMutableList()
        if (node.isArray) {
            node.forEachIndexed { index, objNode ->
                val ingredient = objNode.get("ingredient")?.asText()
                val cooking_technique = objNode.get("cooking_technique")?.asText()
                val description = objNode.get("description").asText()
                suggestions.add(HelperSuggestion(ingredient, cooking_technique, description))
            }
            return suggestions
        } else {
            throw Exception("Invalid helper response")
        }
    }

    /////////////////////////////////////////////////////////
    // Common
    ////////////////////////////////////////////////////////

    private fun extractJsonString(text: String): String? {
        val startToken = "```json"
        val endToken = "```"
        // Find beginning of JSON block
        val startIndex = text.indexOf(startToken)
        if (startIndex == -1) return null
        // Find end of JSON block
        val endIndex = text.indexOf(endToken, startIndex + startToken.length)
        if (endIndex == -1) return null
        // Extract JSON from string
        return text.substring(startIndex + startToken.length, endIndex)
    }
}