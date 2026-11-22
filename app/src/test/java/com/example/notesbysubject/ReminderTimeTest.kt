package com.example.notesbysubject

import com.example.notesbysubject.reminders.nextReminderDelay
import java.time.ZonedDateTime
import org.junit.Assert.*
import org.junit.Test

class ReminderTimeTest {
    @Test fun passedTimeSchedulesTomorrow() {
        val now = ZonedDateTime.parse("2026-11-22T19:00:00+01:00[Europe/Madrid]")
        assertEquals(23, nextReminderDelay(now, 18, 0).toHours())
    }

    @Test fun upcomingTimeSchedulesToday() {
        val now = ZonedDateTime.parse("2026-11-22T17:30:00+01:00[Europe/Madrid]")
        assertEquals(30, nextReminderDelay(now, 18, 0).toMinutes())
    }

    @Test fun tomorrowUsesLocalClockAcrossDaylightSaving() {
        val now = ZonedDateTime.parse("2026-03-28T19:00:00+01:00[Europe/Madrid]")
        assertEquals(22, nextReminderDelay(now, 18, 0).toHours())
    }
}
