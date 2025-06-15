package com.pangaea.idothecooking.ui.shared

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.utils.extensions.readContentFromAssets
import com.pangaea.idothecooking.utils.extensions.replaceVariables

class AboutDialog() : DialogFragment() {
    @SuppressLint("CommitPrefEdits")
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.about_view, null, false)!!
        val webView = layout.findViewById<WebView>(R.id.viewport)
        val data: String = requireContext().readContentFromAssets("about.html")

        // Variable replacement routine
        val formattedHTML = data.replaceVariables(requireContext(), resources)

        webView.loadDataWithBaseURL(null, formattedHTML, "text/html", "utf-8", null);
        val recipeView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        recipeView.setView(layout)
            .setNegativeButton(R.string.close) { dialog, _ ->
                dialog.cancel()
            }
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (sharedPreferences.getBoolean("auto_launch_about", true)) {
            recipeView.setNeutralButton(R.string.do_not_show_about_at_startup) { dialog, _ ->
                val s = sharedPreferences.edit()
                s.putBoolean("auto_launch_about", false)
                s.apply()
                dialog.cancel()
            }
        }

        return recipeView.create()
    }
}
