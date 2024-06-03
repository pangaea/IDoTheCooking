package com.pangaea.idothecooking.utils

import java.util.Timer
import java.util.TimerTask

class ThrottledUpdater {
    private val delayMillis: Long = 500 // Milliseconds
    private var delayTimer = Timer()
    fun delayedUpdate(lambda: () -> Unit) {
        synchronized(this) { delayTimer.cancel() }
        delayTimer = Timer()
        delayTimer.schedule(
            object : TimerTask() {
                override fun run() {
                    synchronized(this) { lambda() }
                }
            }, delayMillis
        )
    }
}