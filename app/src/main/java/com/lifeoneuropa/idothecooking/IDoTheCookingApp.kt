package com.lifeoneuropa.idothecooking

import android.app.Application
import com.lifeoneuropa.idothecooking.state.db.AppDatabase

class IDoTheCookingApp : Application() {
    var startupMode = true
    fun getDatabase(): AppDatabase {
        return AppDatabase.getDatabase(this)
    }
}