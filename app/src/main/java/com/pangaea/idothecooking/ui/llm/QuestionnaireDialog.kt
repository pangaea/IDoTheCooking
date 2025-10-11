package com.pangaea.idothecooking.ui.llm

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.textfield.TextInputEditText
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.ui.shared.ImageTool
import com.pangaea.idothecooking.utils.extensions.readContentFromAssets

class QuestionnaireDialog(val callback: (results: Map<String, String>) -> Unit)
	: DialogFragment() {
	private val answers = emptyMap<String, String>()

	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		val layout: View = requireActivity().layoutInflater.inflate(R.layout.questionnaire_view,
																	null, false)!!
		val json: String = requireContext().readContentFromAssets("questionnaire.json")
		val mapper = ObjectMapper()
		val qData: JsonNode = mapper.readTree(json)
		val typesNode: JsonNode? = qData.get("types")
		if (typesNode != null && typesNode.isArray) {
			val types = emptyList<String>().toMutableList()
			for (objNode in typesNode) {
				types.add(objNode.get("name").textValue())
			}
			renderQuestion(layout, "Choose a recipe type?", types)
		}
		else {
			throw Exception("Invalid questionnaire file")
		}

		val recipeView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
		recipeView.setView(layout)
			.setPositiveButton("Next") { _, _ ->
				callback(answers)
			}
			.setNegativeButton(R.string.close) { dialog, _ ->
				dialog.cancel()
			}
		return recipeView.create()
	}

	private fun renderQuestion(layout: View, question: String, answers: List<String>) {

		// Render question
		val nameView = layout.findViewById<TextView>(R.id.question)
		nameView.text = question

		// Render answers
		val answersView = layout.findViewById<LinearLayout>(R.id.answers)
		answersView.removeAllViews()
		for (answer in answers) {
			val answerView: View = requireActivity().layoutInflater.inflate(R.layout.questionnaire_answer_view,
																		null, false)!!
			val labelView = answerView.findViewById<TextView>(R.id.label)
			labelView.text = answer

			val imageView = answerView.findViewById<ImageView>(R.id.image)
			//imageView.setImageURI(Uri("asset://image_library/SausagePeppers.jpg"))
			ImageTool(requireActivity()).display(imageView, "asset://image_library/SausagePeppers.jpg")

			answersView.addView(answerView)
		}
	}
}
