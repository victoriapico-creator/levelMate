package com.example.notesbysubject.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notesbysubject.data.settings.AppSettings
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

const val REMINDER_CHANNEL = "project_deadlines"
private const val REMINDER_WORK = "levelmate-daily-reminder"

object ReminderScheduler {
    fun sync(context: Context, settings: AppSettings, replace: Boolean = false) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(
            REMINDER_CHANNEL, "Project reminders", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Projects with an unfinished deadline today or earlier" })
        val workManager = WorkManager.getInstance(context)
        if (!settings.remindersEnabled) {
            workManager.cancelUniqueWork(REMINDER_WORK)
            manager.cancel(1001)
            return
        }
        val delay = nextReminderDelay(ZonedDateTime.now(), settings.reminderHour, settings.reminderMinute)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .addTag(REMINDER_WORK).build()
        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK,
            if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
