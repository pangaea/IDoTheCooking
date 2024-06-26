package com.pangaea.idothecooking.ui.shared

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R

class PicklistDlg(val title: String, val options: List<String>,
                  val callback: (selection: String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val listItems = options.toTypedArray()
        var selectedItem: String = listItems[0]
        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setCancelable(false)
            .setSingleChoiceItems(
                listItems, 0,
            ) { dialog, which ->
                selectedItem = listItems[which]
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                callback(selectedItem)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
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