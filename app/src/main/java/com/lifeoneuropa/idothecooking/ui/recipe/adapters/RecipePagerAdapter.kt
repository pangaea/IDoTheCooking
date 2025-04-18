package com.lifeoneuropa.idothecooking.ui.recipe.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lifeoneuropa.idothecooking.ui.recipe.RecipeDirectionsFragment
import com.lifeoneuropa.idothecooking.ui.recipe.RecipeIngredientsFragment
import com.lifeoneuropa.idothecooking.ui.recipe.RecipeMainFragment
import com.lifeoneuropa.idothecooking.ui.recipe.HelperSearchFragment

class RecipePagerAdapter(
    fragmentManager: FragmentManager,
    private val totalTabs: Int,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = totalTabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RecipeMainFragment.newInstance()
            1 -> RecipeIngredientsFragment.newInstance()
            2 -> RecipeDirectionsFragment.newInstance()
            3 -> HelperSearchFragment.newInstance()
            else -> RecipeMainFragment.newInstance()
        }
    }
}