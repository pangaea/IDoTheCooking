package com.pangaea.idothecooking.ui.shared

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.utils.extensions.readContentFromAssets

class AboutDialog() : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.about_view, null, false)!!
        val webView = layout.findViewById<WebView>(R.id.viewport)
        val data: String = requireContext().readContentFromAssets("about.html")
        webView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
        val recipeView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        recipeView.setView(layout)
            .setNegativeButton(R.string.close) { dialog, _ ->
                dialog.cancel()
            }
        return recipeView.create()
    }
}
