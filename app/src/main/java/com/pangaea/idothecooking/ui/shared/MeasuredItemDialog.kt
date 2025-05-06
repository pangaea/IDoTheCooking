package com.pangaea.idothecooking.ui.shared

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.MeasuredItem
import com.pangaea.idothecooking.utils.extensions.focusAndShowKeyboard
import com.pangaea.idothecooking.utils.extensions.fractions
import com.pangaea.idothecooking.utils.extensions.setVulgarFraction
import com.pangaea.idothecooking.utils.extensions.vulgarFraction
import com.robertlevonyan.views.expandable.Expandable


class MeasuredItemDialog(val resInt: Int,
                         private val measuredItem: MeasuredItem?,
                         private val listenerOk: (measuredItem: MeasuredItem) -> Unit,
                         private val listenerCancel: DialogInterface.OnClickListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val layout: View = requireActivity().layoutInflater.inflate(R.layout.measured_item_edit,
                                                                    null, false)!!

        // Get text views
        val amountWholeView = layout.findViewById<NumberPicker>(R.id.amount_whole)
//        val amountWholeView = layout.findViewById<EditText>(R.id.amount_whole)
        val amountFractionView = layout.findViewById<NumberPicker>(R.id.amount_fraction)
        val unitView = layout.findViewById<TextView>(R.id.unit)
        val nameView = layout.findViewById<TextView>(R.id.name)

        amountWholeView.minValue = 0
        amountWholeView.maxValue = 100

        val reversedFractions = fractions.reversedArray().toMutableList()
        //reversedFractions.removeLast()
        reversedFractions[0] = ".0"
        amountFractionView.minValue = 0
        amountFractionView.maxValue = reversedFractions.size - 1
        amountFractionView.displayedValues = reversedFractions.toTypedArray()

        val expandablePanel: Expandable = layout.findViewById<Expandable>(R.id.expandable)
        val ingredientView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        var buttonText = resources.getString(R.string.update)
        if (measuredItem != null) {
            if (measuredItem.amount != null && measuredItem.amount!! > 0) {
                val wholeNum = measuredItem.amount!!.toInt() ?: 0
                amountWholeView.value = wholeNum
//                amountWholeView.setText(wholeNum.toString())
                if (measuredItem.amount!! - wholeNum == 0.0) {
                    amountFractionView.value = 0
                } else {
                    amountFractionView.value =
                        reversedFractions.indexOf((measuredItem.amount!! - wholeNum).vulgarFraction.first)
                }
                expandablePanel.expand()
            } else {
                expandablePanel.collapse()
            }

            //val unitView: TextView = layout.findViewById(R.id.unit)
            unitView.text = measuredItem.unit

            nameView.text = measuredItem.name
            //ingredientView.setTitle(resources.getString(R.string.ingredient_edit_title))
        } else {
            //ingredientView.setTitle(resources.getString(R.string.ingredient_new_title))
            buttonText = resources.getString(R.string.add)

            // Total hack. Fix this later
            if (resInt == R.string.ingredient_new_title) {
                expandablePanel.expand()
            } else {
                expandablePanel.collapse()
            }
        }
        ingredientView.setTitle(resources.getString(resInt))
        ingredientView.setView(layout)
            .setPositiveButton(
                buttonText
            ) { dialog, id ->
                val amount: Double = setVulgarFraction(amountWholeView?.value!!, amountFractionView.value)
                val obj = MeasuredItem()
                obj.name = nameView.text.toString()
                obj.amount = amount
                obj.unit = unitView.text.toString()
                listenerOk(obj)
            }
            .setNegativeButton(
                R.string.cancel,
                listenerCancel
            )
        //nameView.requestFocus()
        nameView.focusAndShowKeyboard()
        return ingredientView.create()
    }

//    override fun onResume() {
//        super.onResume()
//        val window = dialog!!.window ?: return
//        val params = window.attributes
////        params.width = WindowManager.LayoutParams.MATCH_PARENT
////        params.height = 1500
//        window.attributes = params
//    }
}