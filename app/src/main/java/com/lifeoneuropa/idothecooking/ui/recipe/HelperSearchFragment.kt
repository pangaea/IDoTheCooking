package com.lifeoneuropa.idothecooking.ui.recipe

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.state.db.entities.Direction
import com.lifeoneuropa.idothecooking.state.db.entities.Ingredient
import com.lifeoneuropa.idothecooking.ui.recipe.adapters.HelperAdapter
import com.lifeoneuropa.idothecooking.ui.recipe.adapters.HelperSuggestion
import com.lifeoneuropa.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.lifeoneuropa.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.lifeoneuropa.idothecooking.ui.recipe.viewmodels.SelectedRecipeModel
import com.lifeoneuropa.idothecooking.ui.shared.adapters.RecycleViewClickListener
import com.lifeoneuropa.idothecooking.utils.connect.LlmGateway
import com.lifeoneuropa.idothecooking.utils.extensions.disable
import com.lifeoneuropa.idothecooking.utils.extensions.enable
import com.lifeoneuropa.idothecooking.utils.extensions.observeOnce
import com.lifeoneuropa.idothecooking.utils.formatting.SuggestionFormatter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HelperSearchFragment : Fragment() {
    private lateinit var recipeViewModel: RecipeViewModel
    private val selectedRecipeModel: SelectedRecipeModel by activityViewModels()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.helper_search_fragment, container, false)
        recipeViewModel = RecipeViewModelFactory(requireActivity().application,
                                                 null).create(RecipeViewModel::class.java)
        val descView = view.findViewById<TextInputEditText>(R.id.recipeDesc)
        val genButton = view.findViewById<MaterialButton>(R.id.generate_recipes)
        val progressBar = view.findViewById<ProgressBar>(R.id.loading_spinner)
        genButton?.setOnClickListener {
            if (descView.text!!.isEmpty()) {
                descView.error = resources.getString(R.string.description_missing)
            } else {
                try {
                    // MASK - View
                    progressBar.visibility = View.VISIBLE
                    genButton.disable()
                    descView.disable()

                    val me: Fragment = this
                    selectedRecipeModel.selectedRecipe.observeOnce { recipe ->
                        GlobalScope.launch {
                            LlmGateway(requireContext()).suggestEnhancements(descView.text.toString(),
                                                                             recipe) { success, suggestions ->
                                requireActivity().runOnUiThread {
                                    // UNMASK - View
                                    progressBar.visibility = View.GONE
                                    genButton.enable()
                                    descView.enable()

                                    if (!success) {
                                        Toast.makeText(context, requireActivity()
                                            .getString(R.string.llm_failure), Toast.LENGTH_LONG).show()
                                    } else {

                                        val list = view.findViewById<RecyclerView>(R.id.list)
                                        with(list) {
                                            layoutManager = LinearLayoutManager(context)
                                            val listener = object : RecycleViewClickListener() {
                                                @SuppressLint("NotifyDataSetChanged")
                                                override fun click(id: Int) {
                                                    val adpt: HelperAdapter =
                                                        (adapter as HelperAdapter)
                                                    val suggestion = adpt.getItem(id)
                                                    val saveChangesAlertBuilder =
                                                        AlertDialog.Builder(requireActivity())
                                                            .setMessage(SuggestionFormatter.formatDisplay(
                                                                context,
                                                                suggestion))
                                                            .setTitle(R.string.helper_prompt_title)
                                                            .setCancelable(true)
                                                            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
                                                            .setPositiveButton(context.getString(R.string.add)) { _, _ ->
                                                                handleSuggestionImport(suggestion)
                                                                adpt.removeAt(id)
                                                                adpt.notifyDataSetChanged()
                                                                Toast.makeText(context,
                                                                               requireActivity()
                                                                                   .getString(R.string.import_complete),
                                                                               Toast.LENGTH_LONG)
                                                                    .show()
                                                            }
                                                    val saveChangesAlert =
                                                        saveChangesAlertBuilder.create()
                                                    saveChangesAlert.show()
                                                }
                                            }
                                            adapter =
                                                HelperAdapter(context,
                                                              suggestions.toMutableList(),
                                                              listener)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch(e: Exception) {
                    // UNMASK - View
                    progressBar.visibility = View.GONE
                    genButton.enable()
                    descView.enable()
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        descView.requestFocus()
        return view
    }

    fun handleSuggestionImport(suggestion: HelperSuggestion) {
        selectedRecipeModel.selectedRecipe.observeOnce { recipe ->
            if (suggestion.ingredient != null) {
                if (!recipe.ingredients.map{it.name}.contains(suggestion.ingredient)) {
                    // Add ingredient if one if specified
                    val mutableIngredients = recipe.ingredients.toMutableList()
                    val ingredient = Ingredient()
                    ingredient.name = suggestion.ingredient!!
                    ingredient.order = 100
                    mutableIngredients.add(ingredient)
                    recipe.ingredients = mutableIngredients
                    selectedRecipeModel.setRecipeDetails(recipe)
                }
            }

            // Add direction to steps
            val mutableDirections = recipe.directions.toMutableList()
            val direction = Direction()
            if (suggestion.cookingTechnique != null) {
                // Prefix the name of cooking technique
                direction.content = suggestion.cookingTechnique!! + ": " + suggestion.description
            } else {
                direction.content = suggestion.description
            }
            direction.order = 100
            mutableDirections.add(direction)
            recipe.directions = mutableDirections
            selectedRecipeModel.setRecipeDetails(recipe)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HelperSearchFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}