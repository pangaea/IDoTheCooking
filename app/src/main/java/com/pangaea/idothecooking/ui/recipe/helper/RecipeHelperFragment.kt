package com.pangaea.idothecooking.ui.recipe.helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.RecipeCallBackListener
import com.pangaea.idothecooking.ui.recipe.helper.adapters.RecipeHelperAdapter
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.utils.extensions.observeOnce

class RecipeHelperFragment : Fragment(), RecipeCallBackListener {
    private lateinit var sectionsPagerAdapter: RecipeHelperAdapter
    private lateinit var viewModel: RecipeViewModel
    private lateinit var recipeDetails: RecipeDetails

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_helper, container, false)

        viewModel = RecipeViewModelFactory(requireActivity().application, 3).create(RecipeViewModel::class.java)
        viewModel.getDetails()?.observeOnce(this) { recipes ->
            recipeDetails = recipes[0]
            sectionsPagerAdapter = RecipeHelperAdapter(parentFragmentManager, 3, lifecycle, recipeDetails)
            val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
            viewPager.isUserInputEnabled = false
            viewPager.adapter = sectionsPagerAdapter

            val prevBtn = view.findViewById<MaterialButton>(R.id.prev)
            prevBtn.setOnClickListener() {
                viewPager.currentItem = viewPager.currentItem - 1
            }

            val nextBtn = view.findViewById<MaterialButton>(R.id.next)
            nextBtn.setOnClickListener() {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }

        return view
    }

    override fun getRecipeDetails(): RecipeDetails {
        return recipeDetails;
    }

    override fun onRecipeInfoUpdate(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override fun onRecipeCategories(categories: List<Category>) {
        TODO("Not yet implemented")
    }

    override fun onRecipeDirectionUpdate(directions: List<Direction>) {
        TODO("Not yet implemented")
    }

    override fun onRecipeIngredientUpdate(ingredients: List<Ingredient>) {
        TODO("Not yet implemented")
    }
}