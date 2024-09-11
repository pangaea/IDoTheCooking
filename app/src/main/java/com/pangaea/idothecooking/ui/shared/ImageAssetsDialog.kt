package com.pangaea.idothecooking.ui.shared

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import java.io.InputStream


class ImageAssetsDialog(private val selection: String?, val callback: (imageUri: String) -> Unit) : DialogFragment() {
    private val radioButtons: MutableList<RadioButton> = emptyList<RadioButton>().toMutableList()
    private var checkedId = 0

    private fun setRadioButton(radioButton: RadioButton) {
        radioButtons.add(radioButton)
        radioButton.setOnCheckedChangeListener(mOnCheckedChangeListener)
    }

    private val mOnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkedId = buttonView.id
            }
            for (radioButton in radioButtons) {
                if (radioButton.id != checkedId) {
                    radioButton.isChecked = false
                }
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.image_asset_library, null, false)!!

        val names = requireActivity().baseContext.assets.list("image_library")
        val imageGroup: LinearLayout = layout.findViewById(R.id.radio_group);
        names?.forEachIndexed { index, s ->
            val row: View =
                requireActivity().layoutInflater.inflate(R.layout.image_asset_item, null, false)!!
            val libraryImage: ImageView = row.findViewById(R.id.library_image);
            val ims: InputStream = requireActivity().baseContext.assets.open("image_library/${s}")
            libraryImage.setImageDrawable(Drawable.createFromStream(ims, null))
            val libraryCheckbox: RadioButton = row.findViewById(R.id.library_checkbox);
            libraryCheckbox.isChecked = selection?.endsWith(s) ?: false
            libraryCheckbox.id = index
            libraryCheckbox.text = s
            setRadioButton(libraryCheckbox)
            imageGroup.addView(row)
        }

        //build the alert dialog child of this fragment
        val imagesView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        imagesView.setView(layout)
            .setPositiveButton(resources.getString(R.string.update)) { dialog, _ ->
                val imageButton: RadioButton = layout.findViewById(checkedId);
                callback(imageButton.text.toString())
                dialog.cancel()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
        return imagesView.create()
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