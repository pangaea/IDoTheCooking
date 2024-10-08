package com.pangaea.idothecooking.utils.extensions

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.InputStreamReader

fun Context.resIdByName(resIdName: String?, resType: String): Int {
    resIdName?.let {
        return resources.getIdentifier(it, resType, packageName)
    }
    throw Resources.NotFoundException()
}

fun Context.readJSONFromAssets(path: String): String {
    val identifier = "[ReadJSON]"
    try {
        val file = assets.open("$path")
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()
        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
            }
        }
        val jsonString = stringBuilder.toString()
        return jsonString
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}