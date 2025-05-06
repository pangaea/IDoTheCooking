package com.pangaea.idothecooking.ui.recipe

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Category


class RecipeFilterDialog(
    val categories: List<Category>,
    private val filteredCategories: List<Int>,
    private val sortBy: RecipesFragment.SortBy,
    val callback: (categories: List<Category>, sortBy: RecipesFragment.SortBy) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.recipe_filter_edit, null, false)!!

        if (sortBy == RecipesFragment.SortBy.Name) {
            val titleRb: RadioButton? = layout.findViewById(R.id.sortName)
            titleRb?.isChecked = true
        } else if (sortBy == RecipesFragment.SortBy.CreatedBy) {
            val createdRb: RadioButton? = layout.findViewById(R.id.sortCreated)
            createdRb?.isChecked = true
        } else {
            val updatedRb: RadioButton? = layout.findViewById(R.id.sortUpdated)
            updatedRb?.isChecked = true
        }

        val categoriesLayout: LinearLayout = layout.findViewById<LinearLayout>(R.id.categoriesLayout);
        for (n in categories.indices){
            val ch = CheckBox(requireActivity())
            ch.isChecked = filteredCategories.contains(categories[n].id)
            ch.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            ch.id = categories[n].id
            ch.text = categories[n].name
            categoriesLayout.addView(ch)
        }

        //build the alert dialog child of this fragment
        val categoryView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        categoryView.setView(layout)
            .setPositiveButton(resources.getString(R.string.update)) { dialog, _ ->
                val selectedCategories = ArrayList<Category>()
                for (n in categories.indices){
                    val ch = categoriesLayout.findViewById<CheckBox>(categories[n].id)
                    if (ch != null && ch.isChecked) {
                        selectedCategories.add(categories[n])
                    }
                }

                var selectedSortBy = RecipesFragment.SortBy.ModifiedBy
                val titleRb: RadioButton? = layout.findViewById(R.id.sortName)
                if (titleRb?.isChecked == true) {
                    selectedSortBy = RecipesFragment.SortBy.Name
                }
                val createdRb: RadioButton? = layout.findViewById(R.id.sortCreated)
                if (createdRb?.isChecked == true) {
                    selectedSortBy = RecipesFragment.SortBy.CreatedBy
                }
                //val updatedRb: RadioButton? = view?.findViewById(R.id.sortUpdated)
                //updatedRb?.isChecked = true

                callback(selectedCategories, selectedSortBy)
                dialog.cancel()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
        return categoryView.create()
    }

//    override fun onResume() {
//        super.onResume()
//        val window = dialog!!.window ?: return
//        val params = window.attributes
//        params.width = WindowManager.LayoutParams.MATCH_PARENT
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT
//        window.attributes = params
//    }
}