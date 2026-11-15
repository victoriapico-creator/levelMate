package com.example.notesbysubject.ui.camera

import kotlin.math.abs
import kotlin.math.atan2

const val LEVEL_TOLERANCE_DEGREES: Float = 1.5f

/**
 * Calculates tilt angle in degrees from accelerometer x and y values.
 * With the phone upright in portrait (x ~ 0, y ~ 9.8), the angle is ~ 0 degrees.
 */
fun calculateTiltAngle(x: Float, y: Float): Float {
    val radians = atan2(x.toDouble(), y.toDouble())
    return Math.toDegrees(radians).toFloat()
}

/**
 * Applies a simple low-pass smoothing filter.
 * [alpha] determines the smoothing weight (0.0 = keep previous, 1.0 = instant new value).
 */
fun applyLowPassFilter(
    newValue: Float,
    previousValue: Float,
    alpha: Float = 0.2f
): Float {
    return previousValue + alpha * (newValue - previousValue)
}

/**
 * Returns true if the given tilt angle in degrees is within tolerance.
 */
fun isWithinTolerance(
    angleDegrees: Float,
    toleranceDegrees: Float = LEVEL_TOLERANCE_DEGREES
): Boolean {
    return abs(angleDegrees) <= toleranceDegrees
}
