package com.pangaea.idothecooking.ui.shared

import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

open class ShareAndPrintActivity : AppCompatActivity() {

    fun sendMessage(title: String, content: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun createWebPrintJob(name: String, webView: WebView) {
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter(name)
        printManager.print(name, printAdapter, PrintAttributes.Builder().build())
    }
}