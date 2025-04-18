package com.lifeoneuropa.idothecooking.utils

import java.util.Timer
import java.util.TimerTask

class ThrottledUpdater(private val delayMillis: Long) {

    constructor() : this(500) {
    }

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