package com.lifeoneuropa.idothecooking.ui.llm

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.ui.llm.adapters.RecipeGeneratorAdapter
import com.lifeoneuropa.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.lifeoneuropa.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.lifeoneuropa.idothecooking.ui.shared.adapters.CreateRecipeAdapter
import com.lifeoneuropa.idothecooking.ui.shared.adapters.RecycleViewClickListener
import com.lifeoneuropa.idothecooking.utils.connect.LlmGateway
import com.lifeoneuropa.idothecooking.utils.extensions.disable
import com.lifeoneuropa.idothecooking.utils.extensions.enable
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
                        LlmGateway(requireContext())
                            .suggestRecipe(descView.text.toString()) { success, generatedRecipes ->
                            requireActivity().runOnUiThread {
                                // UNMASK - View
                                progressBar.visibility = View.GONE
                                fab.enable()
                                descView.enable()

                                if (!success) {
                                    Toast.makeText(context, requireActivity()
                                        .getString(R.string.llm_failure), Toast.LENGTH_LONG).show()
                                } else {
                                    val list = view.findViewById<RecyclerView>(R.id.list)
                                    with(list) {
                                        layoutManager = LinearLayoutManager(context)
                                        val listener = object : RecycleViewClickListener() {
                                            @RequiresApi(Build.VERSION_CODES.N)
                                            @SuppressLint("NotifyDataSetChanged")
                                            override fun click(id: Int) {
                                                val adpt: RecipeGeneratorAdapter =
                                                    (adapter as RecipeGeneratorAdapter)
                                                val generatedRecipe = adpt.getItem(id)

                                                ViewRecipeDialog(generatedRecipe) { recipe ->
                                                    CreateRecipeAdapter(me.requireActivity(),
                                                                        me.requireContext(),
                                                                        me,
                                                                        me.childFragmentManager,
                                                                        recipeViewModel) { _ ->
                                                        adpt.removeAt(id)
                                                        adpt.notifyDataSetChanged()
                                                        Toast.makeText(context, requireActivity()
                                                            .getString(R.string.import_recipe_complete)
                                                            .replace("{0}", recipe.recipe.name),
                                                                       Toast.LENGTH_LONG).show()
                                                    }.attemptRecipeInsert(recipe)
                                                }.show(childFragmentManager, null)
                                            }
                                        }
                                        adapter =
                                            RecipeGeneratorAdapter(generatedRecipes.toMutableList(),
                                                                   listener)
                                    }
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