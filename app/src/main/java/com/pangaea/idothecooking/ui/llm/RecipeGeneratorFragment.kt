package com.pangaea.idothecooking.ui.llm

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

    @SuppressLint("Range")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_recipe_generator, container, false)
        recipeViewModel = RecipeViewModelFactory(requireActivity().application,
                                                 null).create(RecipeViewModel::class.java)
        val descView = view.findViewById<TextInputEditText>(R.id.recipeDesc)
        val questionnaireBtn = view.findViewById<MaterialButton>(R.id.launch_questionnaire)
        questionnaireBtn?.setOnClickListener {
            QuestionnaireDialog() { answers ->
                val prompt = answers.map {"${it.key}: ${it.value}"}.toString()
                callLLM(view, prompt)
            }.show(childFragmentManager, null)
        }
        val fab = view.findViewById<MaterialButton>(R.id.generate_recipes)
        fab?.setOnClickListener {
            if (descView.text!!.isEmpty()) {
                descView.error = resources.getString(R.string.description_missing)
            } else {
                callLLM(view, descView.text.toString())
            }
        }

        descView.requestFocus()
        return view
    }

    private fun callLLM(view: View, prompt: String) {
        val descView = view.findViewById<TextInputEditText>(R.id.recipeDesc)
        val fab = view.findViewById<MaterialButton>(R.id.generate_recipes)
        val questionnaireBtn = view.findViewById<MaterialButton>(R.id.launch_questionnaire)
        val progressBar = view.findViewById<ProgressBar>(R.id.loading_spinner)
        try {
            // MASK - View
            progressBar.visibility = View.VISIBLE
            fab.disable()
            descView.disable()
            questionnaireBtn.disable()

            val me: Fragment = this
            GlobalScope.launch {
                LlmGateway(requireContext())
                    .suggestRecipe(prompt) { success, generatedRecipes ->
                        requireActivity().runOnUiThread {
                            // UNMASK - View
                            progressBar.visibility = View.GONE
                            fab.enable()
                            descView.enable()
                            questionnaireBtn.enable()

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
            questionnaireBtn.enable()
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}