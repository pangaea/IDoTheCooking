package com.pangaea.idothecooking.ui.recipe

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.utils.extensions.fractionValues
import com.pangaea.idothecooking.utils.extensions.fractions
import com.pangaea.idothecooking.utils.extensions.vulgarFraction


class RecipeIngredientDialog(private val ingredient: Ingredient?,
    private val listenerOk: (ingredient: Ingredient) -> Unit,
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

        val ingredientView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())

        if (ingredient != null) {
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
            ingredientView.setTitle("Edit Ingredient")
        } else {
            ingredientView.setTitle("New Ingredient")
        }
        ingredientView.setView(layout)
            .setPositiveButton(
                R.string.update
            ) { dialog, id ->
                val wholeNumView =
                    (dialog as AlertDialog).findViewById<View>(R.id.amount_whole) as NumberPicker?
                var amount: Double = wholeNumView?.value?.toDouble() ?: 0.00
                val fractionView =
                    (dialog as AlertDialog).findViewById<View>(R.id.amount_fraction) as NumberPicker?
                val reversedFractionValues = fractionValues.reversedArray()
                if (fractionView != null && reversedFractionValues.get(fractionView.value) < 1.00) {
                    amount = amount + reversedFractionValues.get(fractionView.value)
                }
                val unitView = (dialog as AlertDialog).findViewById<View>(R.id.unit) as EditText?
                val nameView = (dialog as AlertDialog).findViewById<View>(R.id.name) as EditText?

                val obj = Ingredient()
                obj.name = nameView?.text.toString()
                obj.amount = amount
                obj.unit = unitView?.text.toString()
                listenerOk(obj)
            }
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