package com.pangaea.idothecooking.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentHomeBinding
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.ui.recipe.RecipeActivity
import com.pangaea.idothecooking.ui.recipe.RecipeViewActivity
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var viewModel: RecipeViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val db: AppDatabase = (activity?.application as IDoTheCookingApp).getDatabase()
        val recipeRepo = db.recipeDao()?.let { RecipeRepository(it) }

        viewModel = recipeRepo?.let { RecipeViewModelFactory(it, null).create(RecipeViewModel::class.java) }!!
        viewModel.getAllRecipesWithDetails().observe(viewLifecycleOwner) { recipes ->
            val linearLayout = root.findViewById<LinearLayout>(R.id.recipeHolder)
            linearLayout.removeAllViews()
            for ((index, recipe: RecipeDetails) in recipes.withIndex()) {
                val recipeLayout: View =
                    requireActivity().layoutInflater.inflate(R.layout.home_recent_recipe,
                                                             null,false)!!
                val image = recipeLayout.findViewById<ImageView>(R.id.recipeImage)
                if (recipe.recipe.imageUri != null && !recipe.recipe.imageUri!!.isEmpty()) {
                    try {
                        Glide.with(requireActivity().baseContext)
                            .load(recipe.recipe.imageUri)
                            .into(image)
                    } catch(_: Exception) {}
                } else {
                    image.visibility = View.GONE
                }

                val content = recipeLayout.findViewById<TextView>(R.id.content)
                content.text = recipe.recipe.name
                val description = recipeLayout.findViewById<TextView>(R.id.description)
                description.text = recipe.recipe.description
                recipeLayout.rootView.setOnClickListener{
                    val intent = Intent(activity, RecipeViewActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", recipe.recipe.id)
                    intent.putExtras(b)
                    startActivity(intent)
                }
                linearLayout.addView(recipeLayout)
                if (index >= 2) break;
            }
        }

        binding.createNewRecipe.setOnClickListener(){
            NameOnlyDialog(R.string.create_recipe_title, null) { name ->
                val recipe = Recipe()
                recipe.name = name
                recipe.description = ""
                val details = RecipeDetails(recipe, emptyList(), emptyList(), emptyList())
                viewModel.insert(details) { id: Long ->
                    val recipeIntent = Intent(activity, RecipeActivity::class.java)
                    val bundle = Bundle()
                    bundle.putInt("id", id.toInt())
                    recipeIntent.putExtras(bundle)
                    startActivity(recipeIntent)
                }
            }.show(childFragmentManager, null)
        }

        binding.viewRecipes.setOnClickListener(){
            activity?.findNavController(R.id.nav_host_fragment_content_main)?.navigate(R.id.nav_recipes)
        }
        binding.viewShoppingLists.setOnClickListener(){
            activity?.findNavController(R.id.nav_host_fragment_content_main)?.navigate(R.id.nav_shopping_lists)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}