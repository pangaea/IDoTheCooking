package com.pangaea.idothecooking.ui.recipe

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink

class RecipeCategoryDialog(val categories: List<Category>,
                           private val selectedCategory: List<RecipeCategoryLink>,
                           val callback: (categories: List<Category>) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val linkedCategoryIds = selectedCategory.map { o -> o.category_id }
        val selectedCategory = BooleanArray(categories.size)
        val categoryArray = categories.mapIndexed { index, o ->
            selectedCategory[index] = linkedCategoryIds.contains(o.id)
            o.name }.toTypedArray()
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.categories_dlg_title)
            .setCancelable(false)
            .setMultiChoiceItems(
                categoryArray,
                selectedCategory
            ) { _, which, isChecked ->
                selectedCategory[which] = isChecked
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                val selectedCategories = ArrayList<Category>()
                for (n in selectedCategory.indices){
                    if (selectedCategory[n]) {
                        val cat = categories[n]
                        selectedCategories.add(cat)
                    }
                }
                callback(selectedCategories)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton(R.string.categories_dlg_clear_all) { _, _ ->
                for (n in selectedCategory.indices){
                    selectedCategory[n] = false
                }
                callback(ArrayList<Category>())
            }
            .create()
    }

    override fun onResume() {
        super.onResume()
        val window = dialog!!.window ?: return
        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params
    }
}