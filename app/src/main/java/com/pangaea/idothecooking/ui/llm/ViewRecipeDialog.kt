package com.pangaea.idothecooking.ui.llm

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.utils.formatting.RecipeRenderer

class ViewRecipeDialog(private val recipeDetails: RecipeDetails, val callback: (recipeDetails: RecipeDetails) -> Unit)
    : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.view_recipe_details, null, false)!!
        val htmlRecipe = RecipeRenderer(requireContext(), recipeDetails, 0, emptyMap()).drawRecipeHtml()
        val webView = layout.findViewById<WebView>(R.id.viewport)
        webView.loadDataWithBaseURL(null, htmlRecipe, "text/html", "utf-8", null);

        val recipeView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        recipeView.setView(layout)
            .setPositiveButton(R.string.import_recipe) {_, _ ->
                callback(recipeDetails)
            }
            .setNegativeButton(R.string.close) { dialog, _ ->
                dialog.cancel()
            }
        return recipeView.create()
    }
}
