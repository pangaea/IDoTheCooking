package com.pangaea.idothecooking.utils.data

import java.util.function.Consumer

interface JsonAsyncImportInterface {
    fun import(json: String, replacementName: String?, ctx: JsonAsyncImportTool.ImportContext,
               callback: Consumer<List<JsonImportTool.ParseLog>>): Boolean
}