package com.lifeoneuropa.idothecooking.ui.shared

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.utils.extensions.readContentFromAssets
import java.io.InputStream

class RecipeTemplateAssetsDialog(private val selection: String?, callback: (imageUri: String) -> Unit)
    : SelectAssetsDialog("recipe_templates", selection, callback) {
    override fun drawRowView(index: Int, fileName: String, imageGroup: LinearLayout) {
        val row: View =
            requireActivity().layoutInflater.inflate(R.layout.recipe_template_asset_item, null, false)!!
        val libraryCheckbox: RadioButton = row.findViewById(R.id.library_checkbox);
        libraryCheckbox.isChecked = selection?.endsWith(fileName) ?: false
        libraryCheckbox.id = index
        //libraryCheckbox.text = fileName
        setRadioButton(libraryCheckbox)

        val json: String? = context?.readContentFromAssets("recipe_templates/${fileName}")
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

                val recipeDesc: TextView = row.findViewById(R.id.recipe_desc);
                recipeDesc.text = objNode.get("description").textValue()
                break
            }
        }
        imageGroup.addView(row)
    }
}