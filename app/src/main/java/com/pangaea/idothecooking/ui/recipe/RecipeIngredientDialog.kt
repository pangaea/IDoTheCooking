package com.pangaea.idothecooking.ui.recipe

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.utils.extensions.fractions
import com.pangaea.idothecooking.utils.extensions.vulgarFraction


class RecipeIngredientDialog(private val ingredient: Ingredient,
    private val listenerOk: DialogInterface.OnClickListener,
    private val listenerCancel: DialogInterface.OnClickListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View =
            requireActivity().layoutInflater.inflate(R.layout.ingredient_edit, null, false)!!

        val amountWholeView = layout.findViewById<NumberPicker>(R.id.amount_whole)
        amountWholeView.minValue = 0
        amountWholeView.maxValue = 100

        val amountView = layout.findViewById<NumberPicker>(R.id.amount_fraction)
        val reversedFractions = fractions.reversedArray().toMutableList()
        reversedFractions.removeLast()
        reversedFractions[0] = ".0"
        amountView.minValue = 0
        amountView.maxValue = reversedFractions.size - 1
        amountView.displayedValues = reversedFractions.toTypedArray()

        if (ingredient.amount != null) {
            val wholeNum = ingredient.amount!!.toInt() ?: 0
            amountWholeView.value = wholeNum
            if (ingredient.amount!! - wholeNum == 0.0) {
                amountView.value = 0
            } else {
                amountView.value =
                    reversedFractions.indexOf((ingredient.amount!! - wholeNum).vulgarFraction.first)
            }
        }

        val unitView: TextView = layout.findViewById(R.id.unit)
        unitView.text = ingredient.unit

        val nameView: TextView = layout.findViewById(R.id.name)
        nameView.text = ingredient.name

        //build the alert dialog child of this fragment
        val ingredientView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        //restore the background_color and layout_gravity that Android strips
        //b.getContext().getTheme().applyStyle(com.pangaea.idothecooking.R.style.RecipeIngredientDialog, true)
        ingredientView.setView(layout)
            .setPositiveButton(
                R.string.update,
                listenerOk
            )
            .setNegativeButton(
                R.string.cancel,
                listenerCancel
            )
        return ingredientView.create()
    }

    override fun onResume() {
        super.onResume()
        val window = dialog!!.window ?: return
        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = 1100
        window.attributes = params
    }
}