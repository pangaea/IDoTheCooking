package com.pangaea.idothecooking.ui.shared

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.textfield.TextInputEditText
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.utils.extensions.readJSONFromAssets
import java.io.InputStream

class CreateRecipeDialog(val callback: (name: String, fileName: String?) -> Unit) : DialogFragment() {
    lateinit var layout: View
    var fileNames: Array<String>? = null
    var displayNames: MutableList<String>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        layout = requireActivity().layoutInflater.inflate(R.layout.create_recipe_form, null, false)!!
        displayNames = mutableListOf<String>()


        fileNames = requireActivity().baseContext.assets.list("recipe_templates")
        val imageGroup: LinearLayout = layout.findViewById(R.id.radio_group);
        fileNames?.forEachIndexed { index, fileName ->
            drawRowView(index, fileName, imageGroup)
        }




        val nameView = layout.findViewById<TextInputEditText>(R.id.name)
        val dlg: AlertDialog = AlertDialog.Builder(requireContext())
        .setTitle(resources.getString(R.string.create_recipe_title))
        .setView(layout)
        .setPositiveButton(resources.getString(R.string.save), null)
        .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel() }.create()

        // Handle validation of form fields
        dlg.setOnShowListener {
            val mPositiveButton = dlg.getButton(AlertDialog.BUTTON_POSITIVE)
            mPositiveButton.setOnClickListener {
                if (nameView.text!!.isEmpty()) {
                    nameView.setError(resources.getString(R.string.recipe_name_missing))
                } else {
                    var fileName: String? = null
                    if(checkedId >= 0) {
                        fileName = fileNames?.get(checkedId)
                    }
                    callback(nameView.text.toString(), fileName)
                    dlg.cancel()
                }
            }
        }
        return dlg;
    }


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
                    val nameView: EditText = layout.findViewById(R.id.name);
                    nameView.setText(displayNames?.get(checkedId))
                    radioButton.isChecked = false
                }
            }
        }

    private fun drawRowView(index: Int, fileName: String, imageGroup: LinearLayout) {
        val row: View =
            requireActivity().layoutInflater.inflate(R.layout.recipe_template_asset_item, null, false)!!
        val libraryCheckbox: RadioButton = row.findViewById(R.id.library_checkbox);
        //libraryCheckbox.isChecked = selection?.endsWith(fileName) ?: false
        libraryCheckbox.id = index
        //libraryCheckbox.text = fileName
        setRadioButton(libraryCheckbox)

        val json: String? = context?.readJSONFromAssets("recipe_templates/${fileName}")
        val mapper = ObjectMapper()
        val node: JsonNode = mapper.readTree(json)
        val recipesNode: JsonNode? = node.get("recipes")
        if (recipesNode != null && recipesNode.isArray) {
            for (objNode in recipesNode) {
                val imageUri = objNode.get("imageUri").textValue()
                val libraryImage: ImageView = row.findViewById(R.id.library_image);
                val ims: InputStream = requireActivity().baseContext.assets.open(imageUri.substring("asset://".length))
                libraryImage.setImageDrawable(Drawable.createFromStream(ims, null))

                val recipeName: TextView = row.findViewById(R.id.recipe_name);
                recipeName.text = objNode.get("name").textValue()
                displayNames?.add(objNode.get("name").textValue())

                val recipeDesc: TextView = row.findViewById(R.id.recipe_desc);
                recipeDesc.text = objNode.get("description").textValue()
                break
            }
        }
        imageGroup.addView(row)
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