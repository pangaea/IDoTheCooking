package com.lifeoneuropa.idothecooking.utils.extensions

import kotlin.math.abs

/**
 * @author aminography
 */

val Double.vulgarFraction: Pair<String, Double>
    get() {
        val whole = toInt()
        val sign = if (whole < 0) -1 else 1
        val fraction = this - whole

        for (i in 1 until fractions.size) {
            if (abs(fraction) > (fractionValues[i] + fractionValues[i - 1]) / 2) {
                if (whole > 0 ) {
                    return if (fractionValues[i - 1] == 1.0) {
                        "${whole + sign}" to (whole + sign).toDouble()
                    } else {
                        "$whole ${fractions[i - 1]}" to whole + sign * fractionValues[i - 1]
                    }
                } else {
                    return if (fractionValues[i - 1] == 1.0) {
                        "" to (whole + sign).toDouble()
                    } else {
                        fractions[i - 1] to whole + sign * fractionValues[i - 1]
                    }
                }
            }
        }
        return "$whole" to whole.toDouble()
    }

fun setVulgarFraction(wholeNum: Int, fractionVal: Int?): Double {
    var amount: Double = wholeNum.toDouble()
    val reversedFractionValues = fractionValues.reversedArray()
    if (fractionVal != null && reversedFractionValues.get(fractionVal) < 1.00) {
        amount += reversedFractionValues[fractionVal]
    }
    return amount
}

val Float.vulgarFraction: Pair<String, Double>
    get() = toDouble().vulgarFraction

val fractions = arrayOf(
    //"",                           // 16/16
    //"\u00B9\u2075/\u2081\u2086",  // 15/16
    //"\u215E",                     // 7/8
    //"\u00B9\u00B3/\u2081\u2086",  // 13/16
    "\u00BE",                     // 3/4
    //"\u00B9\u00B9/\u2081\u2086",  // 11/16
    "\u2154",                     // 2/3
    //"\u215D",                     // 5/8
    //"\u2079/\u2081\u2086",        // 9/16
    "\u00BD",                     // 1/2
    //"\u2077/\u2081\u2086",        // 7/16
    //"\u215C",                     // 3/8
    "\u2153",                     // 1/3
    //"\u2075/\u2081\u2086",        // 5/16
    "\u00BC",                     // 1/4
    //"\u00B3/\u2081\u2086",        // 3/16
    "\u215B",                     // 1/8
    "\u00B9/\u2081\u2086",        // 1/16
    ""                            // 0/16
)

val fractionValues = arrayOf(
    //1.0,
    //15.0 / 16,
    //7.0 / 8,
    //13.0 / 16,
    3.0 / 4,
    //11.0 / 16,
    2.0 / 3,
    //5.0 / 8,
    //9.0 / 16,
    1.0 / 2,
    //7.0 / 16,
    //3.0 / 8,
    1.0 / 3,
    //5.0 / 16,
    1.0 / 4,
    //3.0 / 16,
    1.0 / 8,
    1.0 / 16,
    0.0
)