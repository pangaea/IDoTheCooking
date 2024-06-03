package com.pangaea.idothecooking.ui.recipe.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.RecipeDirectionsFragment
import com.pangaea.idothecooking.ui.recipe.RecipeIngredientsFragment
import com.pangaea.idothecooking.ui.recipe.RecipeMainFragment

class RecipePagerAdapter(
    fragmentManager: FragmentManager,
    private val totalTabs: Int,
    lifecycle: Lifecycle,
    val recipe: RecipeDetails
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = totalTabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RecipeMainFragment.newInstance(recipe)
            1 -> RecipeIngredientsFragment.newInstance(recipe)
            2 -> RecipeDirectionsFragment.newInstance(recipe)
            else -> RecipeMainFragment.newInstance(recipe)
        }
    }

}