package com.pangaea.idothecooking.ui.shared

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.utils.extensions.focusAndShowKeyboard

class BackDataDlg() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.backup_data_modal, null, false)!!
        val dlg: AlertDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.alert_data_backup_title))
            .setView(layout)
            .setPositiveButton(getString(R.string.alert_data_backup_button)) { dialog, _ ->
                activity?.findNavController(R.id.nav_host_fragment_content_main)
                    ?.navigate(R.id.nav_backup_restore)
            }
            .setNegativeButton(getString(R.string.alert_data_backup_later)) { dialog, _ ->
                dialog.cancel() }
            .create()

        return dlg;
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