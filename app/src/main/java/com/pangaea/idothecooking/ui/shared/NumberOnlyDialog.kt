package com.pangaea.idothecooking.ui.shared

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R

class NumberOnlyDialog(private val resInt: Int, private val number: Int?,
                       val callback: (number: Int) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.number_only_form, null, false)!!
        val numberView = layout.findViewById<NumberPicker>(R.id.number)
        numberView.minValue = 1
        numberView.maxValue = 100
        if (number != null) {
            numberView.value = number
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(resInt))
            .setView(layout)
            .setPositiveButton(resources.getString(R.string.update)) { _, _ ->
                callback(numberView.value)
            }
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