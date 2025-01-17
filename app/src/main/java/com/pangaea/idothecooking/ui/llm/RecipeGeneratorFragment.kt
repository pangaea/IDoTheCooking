package com.pangaea.idothecooking.ui.llm

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.ui.llm.adapters.RecipeGeneratorAdapter
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shared.adapters.CreateRecipeAdapter
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener
import com.pangaea.idothecooking.utils.connect.LlmGateway
import com.pangaea.idothecooking.utils.extensions.disable
import com.pangaea.idothecooking.utils.extensions.enable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class RecipeGeneratorFragment : Fragment() {
    private lateinit var recipeViewModel: RecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("Range")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_recipe_generator, container, false)
        recipeViewModel = RecipeViewModelFactory(requireActivity().application,
                                                 null).create(RecipeViewModel::class.java)
        val descView = view.findViewById<TextInputEditText>(R.id.recipeDesc)
        val fab = view.findViewById<MaterialButton>(R.id.generate_recipes)
        val progressBar = view.findViewById<ProgressBar>(R.id.loading_spinner)
        fab?.setOnClickListener {
            if (descView.text!!.isEmpty()) {
                descView.error = resources.getString(R.string.description_missing)
            } else {
                try {
                    // MASK - View
                    progressBar.visibility = View.VISIBLE
                    fab.disable()
                    descView.disable()

                    val me: Fragment = this
                    GlobalScope.launch {
                        LlmGateway(requireContext()).suggestRecipe(descView.text.toString()) { generatedRecipes ->
                            requireActivity().runOnUiThread {
                                // UNMASK - View
                                progressBar.visibility = View.GONE
                                fab.enable()
                                descView.enable()

                                val list = view.findViewById<RecyclerView>(R.id.list)
                                with(list) {
                                    layoutManager = LinearLayoutManager(context)
                                    val listener = object : RecycleViewClickListener() {
                                        override fun click(id: Int) {
                                            ViewRecipeDialog(generatedRecipes[id]) { recipe ->
                                                CreateRecipeAdapter(me, recipeViewModel).attemptRecipeInsert(recipe)
                                            }.show(childFragmentManager, null)
                                        }
                                    }
                                    adapter = RecipeGeneratorAdapter(generatedRecipes,
                                                                     listener,
                                                                     requireActivity())
                                }
                            }
                        }
                    }
                } catch(e: Exception) {
                    // UNMASK - View
                    progressBar.visibility = View.GONE
                    fab.enable()
                    descView.enable()

                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        descView.requestFocus()
        return view
    }
}