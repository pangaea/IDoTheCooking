package com.pangaea.idothecooking.ui.shared

import android.app.Activity
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.InputStream

class ImageTool(private val imageView: ImageView, private val activity: Activity) {
    companion object {
        val assetProtocol = "asset://"
        val contentProtocol = "content://"
    }
    fun display(uri: String) {
        if (uri.startsWith(assetProtocol)) {
            //val names = activity.baseContext.assets.list("image_library")
            val assetName = uri.substring(assetProtocol.length)
            val ims: InputStream = activity.baseContext.assets.open(assetName)
            imageView.setImageDrawable(Drawable.createFromStream(ims, null))
        } else {
            try {
                activity.let { it1 ->
                    Glide.with(it1.baseContext)
                        .load(uri)
                        .into(imageView)
                }
            } catch(_: Exception) {}
        }
    }
}