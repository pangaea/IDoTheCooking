package com.pangaea.idothecooking

import android.app.Application
import android.os.Bundle
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.ui.shared.AboutDialog

class IDoTheCookingApp : Application() {
    var startupMode = true
    fun getDatabase(): AppDatabase {
        return AppDatabase.getDatabase(this)
    }
}