package com.pangaea.idothecooking.ui.recipe.helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.RecipeDetails

class OptionsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.helper_options_fragment, container, false)
        return view;
    }

    companion object {
        @JvmStatic
        fun newInstance(recipe: RecipeDetails) =
            OptionsFragment().apply {
                arguments = Bundle().apply {
                    //putStringArrayList(RECIPE_INGREDIENTS, ArrayList(recipe.ingredients.map() { it.name}))
                }
            }
    }
}