package com.pangaea.idothecooking.ui.llm

interface QuestionnaireCallback {
	fun selectOption(name: String, fileName: String?)
}