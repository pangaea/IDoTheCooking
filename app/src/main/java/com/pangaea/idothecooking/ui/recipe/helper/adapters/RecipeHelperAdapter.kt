package com.pangaea.idothecooking.ui.recipe.helper.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.RecipeIngredientsFragment
import com.pangaea.idothecooking.ui.recipe.helper.OptionsFragment
import com.pangaea.idothecooking.ui.recipe.helper.SearchFragment

class RecipeHelperAdapter (
    fragmentManager: FragmentManager,
    private val totalTabs: Int,
    lifecycle: Lifecycle,
    val recipe: RecipeDetails
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int = totalTabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SearchFragment.newInstance(recipe)
                1 -> OptionsFragment.newInstance(recipe)
                2 -> RecipeIngredientsFragment.newInstance(recipe)
                else -> SearchFragment.newInstance(recipe)
            }
        }

    }