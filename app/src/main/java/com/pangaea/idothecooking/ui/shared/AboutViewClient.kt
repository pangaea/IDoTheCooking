package com.pangaea.idothecooking.ui.shared

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient

class AboutViewClient(val activity: Activity?) : WebViewClient() {

	@Deprecated("Deprecated in Java", ReplaceWith("true"))
	override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
		// The only link on the page so, we can just assume
		Intent(Intent.ACTION_VIEW, Uri.parse("http://www.flaticon.com")).apply {
			activity?.startActivity(this)
		}
		return true
	}
}