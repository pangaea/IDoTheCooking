package com.lifeoneuropa.idothecooking.ui.shared

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.utils.extensions.focusAndShowKeyboard

class NameOnlyDialog(val resInt: Int, val name: String?, val callback: (name: String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.name_only_form, null, false)!!
        val nameView = layout.findViewById<TextInputEditText>(R.id.name)
        if (name != null) {
            nameView.setText(name)
        }
        val dlg: AlertDialog = AlertDialog.Builder(requireContext())
        .setTitle(resources.getString(resInt))
        .setView(layout)
        .setPositiveButton(resources.getString(R.string.save), null)
        .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel() }
            .create()

        // Handle validation of form fields
        dlg.setOnShowListener {
            val mPositiveButton = dlg.getButton(AlertDialog.BUTTON_POSITIVE)
            mPositiveButton.setOnClickListener {
                if (nameView.text!!.isEmpty()) {
                    nameView.setError(resources.getString(R.string.recipe_name_missing))
                } else {
                    callback(nameView.text.toString())
                    dlg.cancel()
                }
            }
        }
        //nameView.requestFocus()
        nameView.focusAndShowKeyboard()
        return dlg;
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