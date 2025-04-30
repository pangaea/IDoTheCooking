package com.pangaea.idothecooking

import android.app.Application
import com.pangaea.idothecooking.state.db.AppDatabase

class IDoTheCookingApp : Application() {
    var startupMode = true
    fun getDatabase(): AppDatabase {
        return AppDatabase.getDatabase(this)
    }
}