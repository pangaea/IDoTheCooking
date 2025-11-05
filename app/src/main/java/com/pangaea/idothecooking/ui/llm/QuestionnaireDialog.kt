package com.pangaea.idothecooking.ui.llm

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.utils.extensions.readContentFromAssets
import com.pangaea.idothecooking.utils.extensions.resIdByName
import java.io.InputStream

class QuestionnaireDialog(val callback: (results: Map<String, String>) -> Unit)
	: DialogFragment() {

	open class Filter {
		lateinit var operator: String
		lateinit var question: String
		lateinit var answers: List<String>
	}
	open class Option {
		lateinit var name: String
		lateinit var labelName: String
		var promptName: String? = null
		lateinit var image: String
		var filter: Filter? = null
	}
	data class RecipeType(
		val questions: List<String>
	) : Option() {
		constructor() : this(emptyList())
	}
	data class Question(
		val labelQuestion: String,
		val promptQuestion: String? = null,
		val answers: List<Option>
	) {
		constructor() : this("", "", emptyList())
	}

	data class Answer(
		val name: String,
		val prompt: String?
	)
	private val typeMap = emptyMap<String, RecipeType>().toMutableMap()
	private val questionMap = emptyMap<String, Question>().toMutableMap()
	private val answerMap = emptyMap<String, Answer>().toMutableMap()
	private lateinit var currentTypeName: String
	private lateinit var firstQuestion: Question
	private var currentStep = 0
	private lateinit var dlg: AlertDialog

	private fun loadConfig(fileName: String): Question? {
		val json: String = requireContext().readContentFromAssets(fileName)
		//val mapper = ObjectMapper()
		val mapper = ObjectMapper().apply {
			configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		}
		val qData: JsonNode = mapper.readTree(json)
		val typesNode: JsonNode? = qData.get("types")
		if (typesNode != null && typesNode.isArray) {
			val options = emptyList<Option>().toMutableList()
			for (objNode in typesNode) {
				options.add(mapper.convertValue(objNode, Option::class.java))

				typeMap[objNode.get("name").textValue()] =
					mapper.convertValue(objNode, RecipeType::class.java)
			}

			val questionsNode: JsonNode? = qData.get("questions")
			if (questionsNode != null && questionsNode.isObject) {
				questionsNode.fieldNames().forEach {
					val qNode = questionsNode.get(it)
					questionMap[it] = mapper.convertValue(qNode, Question::class.java)
				}
			}
			firstQuestion = Question("questionnaire_choose_type", "type", options)
			return firstQuestion
		}
		return null
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
		val layout: View = requireActivity().layoutInflater.inflate(R.layout.questionnaire_view,
																	null, false)!!
		val recipeView: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
		recipeView.setView(layout)
			.setNegativeButton(R.string.close) { dialog, _ ->
				dialog.cancel()
			}
			.setNeutralButton(R.string.back, null)
		dlg = recipeView.create()

		dlg.setOnShowListener {
			val mNeutralButton = dlg.getButton(AlertDialog.BUTTON_NEUTRAL)
			mNeutralButton.setOnClickListener {
				if (currentStep > 0) {
					gotoQuestionByIndex(layout, --currentStep)
				}
			}
			mNeutralButton?.visibility = View.GONE
		}

		val q = loadConfig("questionnaire.json")
		if (q != null) {
			gotoQuestionByIndex(layout, 0)
		}
		else {
			throw Exception("Invalid questionnaire file")
		}

		return dlg
	}

	fun fillInString(textView: TextView, resName: String) {
		try {
			textView.text = resources.getString(requireContext().resIdByName(resName, "string"))
		}
		catch (e: Exception) {
			textView.text = resName
		}
	}

	fun fillInImage(imageView: ImageView, answer: Option) {
		try {
			val ims: InputStream = requireActivity().baseContext.assets.open("questionnaire_images/${answer.image}.png")
			if (ims != null) {
				imageView.setImageDrawable(Drawable.createFromStream(ims, null))
			}
			else {
				imageView.visibility = View.GONE
			}
		}
		catch (e: Exception) {
			imageView.visibility = View.GONE
		}
	}

	@RequiresApi(Build.VERSION_CODES.N)
	private fun renderQuestion(layout: View, question: Question) {

		// Render question
		val nameView = layout.findViewById<TextView>(R.id.question)
		fillInString(nameView, question.labelQuestion)

		// Render answers
		val answersView = layout.findViewById<LinearLayout>(R.id.answers)
		answersView.removeAllViews()
		for (answer in question.answers) {
			val filter = answer.filter
			if (filter == null ||
				!answerMap.containsKey(filter.question) ||
				filterCondition(filter)) {

				val answerView: View =
					requireActivity().layoutInflater.inflate(R.layout.questionnaire_answer_view,
															 null, false)!!
				val labelView = answerView.findViewById<TextView>(R.id.label)
				fillInString(labelView, answer.labelName)

				val imageView = answerView.findViewById<ImageView>(R.id.image)
				fillInImage(imageView, answer)

				val answerButton = answerView.findViewById<LinearLayout>(R.id.answer_button)
				answerButton.setOnClickListener {
					processClick(layout, answer)
				}

				answersView.addView(answerView)
			}
		}
	}

	fun filterCondition(filter: Filter): Boolean {
		if (filter.operator.equals("excludes")) {
			return !filter.answers.contains(answerMap[filter.question]?.name)
		} else {
			return filter.answers.contains(answerMap[filter.question]?.name)
		}
	}

	@RequiresApi(Build.VERSION_CODES.N)
	private fun gotoQuestionByIndex(layout: View, index: Int) {
		currentStep = index
		//val backButton = layout.findViewById<Button>(R.id.back)
		val backButton = dlg.getButton(AlertDialog.BUTTON_NEUTRAL)
		if (currentStep == 0) {
			renderQuestion(layout, firstQuestion)
			backButton?.visibility = View.GONE
		}
		else {
			backButton?.visibility = View.VISIBLE
			val questionIndex = currentStep - 1
			val questions = typeMap[currentTypeName]?.questions
			if (questions?.size!! > questionIndex) {
				val qName = typeMap[currentTypeName]?.questions?.get(questionIndex)
				questionMap[qName]?.let { renderQuestion(layout, it) }
			} else {
				val answerMap2 = emptyMap<String, String>().toMutableMap()
				answerMap.forEach { question, answer ->
					if (question == "type") {
						answerMap2["type"] = answer.prompt.toString()
					} else {
						val q = questionMap[question]
						if (q?.promptQuestion != null && answer.prompt != null) {
							answerMap2[q.promptQuestion] = answer.prompt
						}
					}
				}
				callback(answerMap2)
				dlg.cancel()
			}
		}
	}

	@RequiresApi(Build.VERSION_CODES.N)
	private fun processClick(layout: View, answer: Option) {
		if (currentStep == 0) {
			currentTypeName = answer.name
			answerMap.clear()
			answerMap["type"] = Answer(answer.name, answer.promptName)
		} else {
			val qName = typeMap[currentTypeName]?.questions?.get(currentStep-1)
			questionMap[qName]?.let {
				if (it.promptQuestion != null) {
					answerMap[qName!!] = Answer(answer.name, answer.promptName)
				}
			}
		}
		gotoQuestionByIndex(layout, ++currentStep)
	}
}
