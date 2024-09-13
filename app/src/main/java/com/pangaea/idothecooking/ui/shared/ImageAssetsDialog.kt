package com.pangaea.idothecooking.ui.shared

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.utils.extensions.resIdByName
import java.io.File
import java.io.InputStream

class ImageAssetsDialog(private val selection: String?, callback: (imageUri: String) -> Unit)
    : SelectAssetsDialog("image_library", selection, callback) {
    override fun drawRowView(index: Int, fileName: String, imageGroup: LinearLayout) {
        val row: View =
            requireActivity().layoutInflater.inflate(R.layout.image_asset_item, null, false)!!
        val libraryImage: ImageView = row.findViewById(R.id.library_image);
        val ims: InputStream = requireActivity().baseContext.assets.open("image_library/${fileName}")
        libraryImage.setImageDrawable(Drawable.createFromStream(ims, null))
        val libraryCheckbox: RadioButton = row.findViewById(R.id.library_checkbox);
        libraryCheckbox.isChecked = selection?.endsWith(fileName) ?: false
        if (libraryCheckbox.isChecked) {
            checkedId = index
        }
        libraryCheckbox.id = index
        val resId = requireActivity().baseContext.resIdByName(File(fileName).nameWithoutExtension, "string")
        if (resId > 0) {
            libraryCheckbox.text = getString(resId)
        } else {
            libraryCheckbox.text = fileName
        }

        setRadioButton(libraryCheckbox)
        imageGroup.addView(row)
    }
}