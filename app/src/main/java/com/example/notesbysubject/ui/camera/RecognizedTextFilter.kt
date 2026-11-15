package com.example.notesbysubject.ui.camera

const val CONFIDENCE_THRESHOLD: Float = 0.7f

data class RecognizedLine(
    val text: String,
    val confidence: Float
)

/**
 * Filters a list of [RecognizedLine] items, returning only those whose confidence
 * is at or above the given [threshold].
 */
fun filterRecognizedLines(
    lines: List<RecognizedLine>,
    threshold: Float = CONFIDENCE_THRESHOLD
): List<RecognizedLine> {
    return lines.filter { it.confidence >= threshold }
}

/**
 * Checks whether a single confidence value meets or exceeds the threshold.
 */
fun isAboveConfidenceThreshold(
    confidence: Float,
    threshold: Float = CONFIDENCE_THRESHOLD
): Boolean {
    return confidence >= threshold
}
