package com.lifeoneuropa.idothecooking

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeoneuropa.idothecooking.utils.extensions.vulgarFraction
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NumberExtTest {

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        val a: String = (1.0).vulgarFraction.first
        val b: String = (15.0 / 16).vulgarFraction.first
        val c: String = (7.0 / 8).vulgarFraction.first
        val d: String = (13.0 / 16).vulgarFraction.first
        val three_quarter: String = (3.0 / 4).vulgarFraction.first
        val e: String = (11.0 / 16).vulgarFraction.first
        val two_third: String = (2.0 / 3).vulgarFraction.first
        val g: String = (5.0 / 8).vulgarFraction.first
        val h: String = (9.0 / 16).vulgarFraction.first
        val half: String = (1.0 / 2).vulgarFraction.first
        val i: String = (7.0 / 16).vulgarFraction.first
        val j: String = (3.0 / 8).vulgarFraction.first
        val third: String = (1.0 / 3).vulgarFraction.first
        val k: String = (5.0 / 16).vulgarFraction.first
        val quarter: String = (1.0 / 4).vulgarFraction.first
        val l: String = (3.0 / 16).vulgarFraction.first
        val m: String = (1.0 / 8).vulgarFraction.first
        val n: String = (1.0 / 16).vulgarFraction.first
        val o: String = (0.0).vulgarFraction.first
        val p: String = (5.66).vulgarFraction.first
    }
}