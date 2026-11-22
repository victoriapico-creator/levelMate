package com.example.notesbysubject.data.settings

data class AppSettings(
    val reminderHour: Int = 18,
    val reminderMinute: Int = 0,
    val levelTolerance: Float = 1.5f,
    val remindersEnabled: Boolean = false
)

fun parseReminderTime(value: String): Pair<Int, Int>? {
    if (!Regex("\\d{2}:\\d{2}").matches(value)) return null
    val hour = value.substringBefore(':').toIntOrNull() ?: return null
    val minute = value.substringAfter(':').toIntOrNull() ?: return null
    return if (hour in 0..23 && minute in 0..59) hour to minute else null
}

fun parseLevelTolerance(value: String): Float? = value.replace(',', '.').toFloatOrNull()
    ?.takeIf { it.isFinite() && it in 0.1f..15f }
