package com.example.notesbysubject

import com.example.notesbysubject.data.settings.parseLevelTolerance
import com.example.notesbysubject.data.settings.parseReminderTime
import org.junit.Assert.*
import org.junit.Test

class SettingsValidationTest {
    @Test fun timeRequiresRealHoursAndMinutes() {
        assertEquals(0 to 0, parseReminderTime("00:00"))
        assertEquals(23 to 59, parseReminderTime("23:59"))
        listOf("24:00", "18:60", "9:00", "abc", "").forEach { assertNull(parseReminderTime(it)) }
    }

    @Test fun toleranceAcceptsCommaAndRejectsNonFiniteValues() {
        assertEquals(1.5f, parseLevelTolerance("1,5"))
        listOf("NaN", "Infinity", "0", "-1", "15.1", "").forEach { assertNull(parseLevelTolerance(it)) }
    }
}
