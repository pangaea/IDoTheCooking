package com.lifeoneuropa.idothecooking.utils.extensions

import android.view.MenuItem

fun MenuItem.setAsEnabled() {
    this.setEnabled(true)
    this.getIcon()?.setAlpha(255)
}

fun MenuItem.setAsDisabled() {
    this.setEnabled(false)
    this.getIcon()?.setAlpha(130)
}