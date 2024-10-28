package com.pangaea.idothecooking.ui.shared

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.pangaea.idothecooking.R

class CreateRecipeDialog(val callback: (name: String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.create_recipe_form, null, false)!!
        val nameView = layout.findViewById<TextInputEditText>(R.id.name)
        return AlertDialog.Builder(requireContext())
        //.setTitle(resources.getString(R.string.create_recipe_title))
        .setTitle(resources.getString(R.string.create_recipe_title))
        .setView(layout)
        .setPositiveButton(resources.getString(R.string.save)) { _, _ ->
            //val name = layout.findViewById<TextInputEditText>(R.id.name)
            callback(nameView.text.toString()) }
        .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel() }
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