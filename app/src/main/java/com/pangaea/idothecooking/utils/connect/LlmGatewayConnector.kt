package com.pangaea.idothecooking.utils.connect

import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.adapters.HelperSuggestion
import com.pangaea.idothecooking.utils.extensions.readContentFromAssets
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class LlmGatewayConnector(val context: Context) {
    val challengeUrl = "http://192.168.1.40/webmenus/LLMGateway/get_challenge"
    val gatewayUrl = "http://192.168.1.40/webmenus/LLMGateway/query"
    private val keyAlias = "attested_key"
    private val mediaTypeJson: MediaType = "application/json".toMediaType()
    private val mockRequest = false

    private fun getCurrentLanguage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault().get(0).displayName
        } else {
            // Fallback for older versions
            Locale.getDefault().displayName
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
	private fun generateAttestedKey(challenge: ByteArray) {
        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

//        val parameterSpec = KeyGenParameterSpec.Builder(
//            keyAlias,
//            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
//        ).run {
//            setDigests(KeyProperties.DIGEST_SHA256)
//            // Passing the challenge enables attestation
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//				setAttestationChallenge(challenge)
//			}
//			build()
//        }

        val parameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).setDigests(KeyProperties.DIGEST_SHA256)
            .setAttestationChallenge(challenge)        // request attestation
            .build()

        kpg.initialize(parameterSpec)
        kpg.generateKeyPair()
    }

//    private fun getCertificateChain(): Array<out Certificate>? {
//        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
//        return keyStore.getCertificateChain(keyAlias)
//    }

    private fun getCertificateChain(): List<X509Certificate>? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        // Retrieve the certificate chain tied to your key alias
        val chain = keyStore.getCertificateChain(keyAlias) ?: return null

        return chain.map { it as X509Certificate }
    }

    private fun convertArrayToJson(cert: List<String>): String {
        // Convert to standard JSON string
        val mapper = ObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        }
        return mapper.writeValueAsString(cert)
    }

    @RequiresApi(Build.VERSION_CODES.O)
	fun convertCertificatesToBase64(certificates: List<X509Certificate>): List<String> {
        val encoder = Base64.getEncoder()
        return certificates.map { cert ->
            encoder.encodeToString(cert.encoded)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
	@Throws(IOException::class)
    fun suggestRecipe(desc: String, callback: (success: Boolean, recipes: List<RecipeDetails>) -> Unit) {
        if (!mockRequest) {
            val promptSuggestRecipe = context.readContentFromAssets("prompts/suggest_recipes.prompt")
            llmRequest(promptSuggestRecipe.replace("{description}", desc)
                           .replace("{language}", getCurrentLanguage())) { success, json ->
                callback(success, json?.let { parseRecipeListJson(it) } ?: emptyList())
            }
        } else {
            // Mock data - for dev
            Thread.sleep(5_000)
            val data: String = context.readContentFromAssets("sample_openai_recipe_list.data")
            callback(true, parseRecipeListJson(data))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun suggestEnhancements(desc: String, recipe: RecipeDetails, callback: (success: Boolean, recipes: List<HelperSuggestion>) -> Unit) {
        if (!mockRequest) {
            val promptSuggestEnhancements = context.readContentFromAssets("prompts/suggest_recipe_improvements.prompt")
            llmRequest(promptSuggestEnhancements.replace("{recipe_name}", recipe.recipe.name)
                           .replace("{ingredient_list}", recipe.ingredients.map{it.name}.joinToString(","))
                           .replace("{requested_improvements}", desc)
                           .replace("{language}", getCurrentLanguage())) { success, json ->
                callback(success, json?.let { parseSuggestionListJson(it) } ?: emptyList())
            }
        } else {
            // Mock data - for dev
            Thread.sleep(5_000)
            val data: String = context.readContentFromAssets("sample_openai_suggestions.data")
            callback(true, parseSuggestionListJson(data))
        }
    }

    val cookieJar = object : CookieJar {
        private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private fun getChallengeFromServer(callback: (success: Boolean, payload: String?) -> Unit) {
        val request: Request = Request.Builder()
            .url(challengeUrl).get().build()
        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(60, TimeUnit.SECONDS) // Set connection timeout to 30 seconds
            .readTimeout(60, TimeUnit.SECONDS)    // Set read timeout to 30 seconds
            .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout to 30 seconds
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    callback(false, null)
                } else {
                    val nonce = response.body!!.string()
                    callback(true, nonce)
                }
            }
        }
        catch (e: Exception) {
            callback(false, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    private fun llmRequest(content: String, callback: (success: Boolean, payload: String?) -> Unit) {
//        val attestationChallenge = ByteArray(16).apply {
//            SecureRandom().nextBytes(this)
//        }
        getChallengeFromServer() { success, attestationChallenge ->

            //val attestationChallenge = "1234567890".toByteArray()
            if (!success || attestationChallenge == null) {
                callback(false, null)
                throw IOException("Failed to get attestation challenge")
            }

            generateAttestedKey(attestationChallenge.toByteArray())
            val ks2 = getCertificateChain()
                ?: throw IOException("Failed to retrieve the attested key")

            // Convert to standard JSON string
            val certList = convertCertificatesToBase64(ks2)
            val jsonString = convertArrayToJson(certList)

            val jsonBody = JSONObject()
            try {
                val jsonMsg = JSONObject()
                jsonMsg.put("content", content)
                val jsonMsgs = JSONArray()
                jsonMsgs.put(jsonMsg)
                jsonBody.put("messages", jsonMsgs)
                val jsonAuth = JSONObject()
                jsonAuth.put("type", "android_attestation")
                jsonAuth.put("data", jsonString)
                jsonBody.put("authentication", jsonAuth)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body: RequestBody = RequestBody.create(mediaTypeJson, jsonBody.toString())
            val request: Request = Request.Builder()
                .url(gatewayUrl)
                .post(body).build()
            val client = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(60, TimeUnit.SECONDS) // Set connection timeout to 30 seconds
                .readTimeout(60, TimeUnit.SECONDS)    // Set read timeout to 30 seconds
                .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout to 30 seconds
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        //throw IOException("Unexpected code $response")
                        callback(false, null)
                    } else {
                        val json = response.body!!.string()
                        //val jsonRoot = JsonParser.parseString(json).asJsonObject;
                        callback(true, "{\"recipes\":$json}")
                    }
                }
            } catch (e: Exception) {
                callback(false, null)
            }
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

    private fun extractJsonString(text: String): String {
        val startToken = "```json"
        val endToken = "```"
        // Find beginning of JSON block
        val startIndex = text.indexOf(startToken)
        if (startIndex == -1) return text
        // Find end of JSON block
        val endIndex = text.indexOf(endToken, startIndex + startToken.length)
        if (endIndex == -1) return text
        // Extract JSON from string
        return text.substring(startIndex + startToken.length, endIndex)
    }
}