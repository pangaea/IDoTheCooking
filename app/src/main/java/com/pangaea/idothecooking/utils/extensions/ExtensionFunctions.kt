package com.pangaea.idothecooking.utils.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.InputStreamReader

fun Context.resIdByName(resIdName: String?, resType: String): Int {
    resIdName?.let {
        return resources.getIdentifier(it, resType, packageName)
    }
    throw Resources.NotFoundException()
}

fun Context.readContentFromAssets(path: String): String {
    try {
        val file = assets.open("$path")
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()
        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
                stringBuilder.append("\n")
            }
        }
        val jsonString = stringBuilder.toString()
        return jsonString
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

@SuppressLint("DiscouragedApi")
fun String.replaceVariables(context: Context): String {
    // Variable replacement routine
    return Regex("\\$\\{([a-zA-Z0-9_]+)\\}").replace(this) { matchResult ->
        val variableName = matchResult.groupValues[1]
        val resourceId = context.resources.getIdentifier(variableName, "string", context.packageName)
        if (resourceId > 0) context.resources.getText(resourceId) else matchResult.value
    }
    /////////////////////////////////////////////////////////////////
}

fun View.disable() {
    background.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    isClickable = false
    isEnabled = false
}
fun View.enable() {
    background.colorFilter = null
    isClickable = true
    isEnabled = true
}
fun View.focusAndShowKeyboard() {
    /**
     * This is to be called when the window already has focus.
     */
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post {
                // We still post the call, just in case we are being notified of the windows focus
                // but InputMethodManager didn't get properly setup yet.
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        // No need to wait for the window to get focus.
        showTheKeyboardNow()
    } else {
        // We need to wait until the window gets focus.
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    // This notification will arrive just before the InputMethodManager gets set up.
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        // It’s very important to remove this listener once we are done.
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

fun View.addBackgroundRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun <T> Activity.startActivityWithBundle(clazz: Class<T>, paramName: String,
                                         paramValue: String) {
    val bundle = Bundle()
    bundle.putString(paramName, paramValue)
    val intent = Intent(this, clazz)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun <T> Activity.startActivityWithBundle(clazz: Class<T>, paramName: String,
                                         paramValue: Int) {
    val bundle = Bundle()
    bundle.putInt(paramName, paramValue)
    val intent = Intent(this, clazz)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun <T> Fragment.startActivityWithBundle(clazz: Class<T>, paramName: String,
                                         paramValue: String) {
    val bundle = Bundle()
    bundle.putString(paramName, paramValue)
    val intent = Intent(activity, clazz)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun <T> Fragment.startActivityWithBundle(clazz: Class<T>, paramName: String,
                                         paramValue: Int) {
    val bundle = Bundle()
    bundle.putInt(paramName, paramValue)
    val intent = Intent(activity, clazz)
    intent.putExtras(bundle)
    startActivity(intent)
}
