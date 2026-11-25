package com.example.notesbysubject.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notesbysubject.MainActivity
import com.example.notesbysubject.R
import com.example.notesbysubject.appContainer
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class ReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        try {
            if (!context.appContainer.settings.settings.first().remindersEnabled) return Result.success()
            val notifications = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) return Result.success()
            if (!notifications.areNotificationsEnabled()) return Result.success()
            val projects = context.appContainer.projects.dao.dueProjects(LocalDate.now().toString())
            if (projects.isEmpty()) { notifications.cancel(1001); return Result.success() }
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val content = projects.take(3).joinToString(", ") { it.name } + if (projects.size > 3) "…" else ""
            val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_reminder)
                .setContentTitle("${projects.size} project(s) need your attention")
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setContentIntent(pendingIntent).setAutoCancel(true).build()
            notifications.notify(1001, notification)
            return Result.success()
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { return if (runAttemptCount < 3) Result.retry() else Result.failure() }
    }
}
