package com.pangaea.idothecooking.ui.shared

import android.content.Context
import android.widget.Toast
import com.pangaea.idothecooking.R

class DisplayException {
    companion object {
        fun show(context: Context, e: Exception) {
            // UNIQUE constraint failed: recipes.name (code 2067 SQLITE_CONSTRAINT_UNIQUE)
            if (e.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true) {
                Toast.makeText(context, R.string.exception_duplicate_name, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, R.string.exception_generic, Toast.LENGTH_LONG).show()
            }
            print(e.message)
        }
    }
}