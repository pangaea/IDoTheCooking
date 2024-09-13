package com.pangaea.idothecooking.ui.shared

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.utils.extensions.resIdByName
import java.io.File
import java.io.InputStream


abstract class SelectAssetsDialog(private val path: String, private val selection: String?,
                                  val callback: (imageUri: String) -> Unit) : DialogFragment() {
    private val radioButtons: MutableList<RadioButton> = emptyList<RadioButton>().toMutableList()
    protected var checkedId = -1

    protected fun setRadioButton(radioButton: RadioButton) {
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

    abstract fun drawRowView(index: Int, fileName: String, imageGroup: LinearLayout)

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.image_asset_library, null, false)!!

        val names = requireActivity().baseContext.assets.list(path)
        val imageGroup: LinearLayout = layout.findViewById(R.id.radio_group);
        names?.forEachIndexed { index, fileName ->
            drawRowView(index, fileName, imageGroup)
        }

        //build the alert dialog child of this fragment
        val imagesView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val dlg: AlertDialog = imagesView.setView(layout)
            .setPositiveButton(R.string.update, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.show()

        // Need to override to validate without closing
        val positiveButton: Button = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener() {
            if(checkedId >= 0) {
                val fileName = names?.get(checkedId)
                if (fileName != null) {
                    callback(fileName)
                }
                dlg.cancel()
            } else {
                Toast.makeText(context, getString(R.string.selection_required), Toast.LENGTH_LONG).show()
            }
        }

        return dlg
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