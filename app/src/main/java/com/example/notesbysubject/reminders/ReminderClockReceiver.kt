package com.example.notesbysubject.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notesbysubject.appContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderClockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return
        val pending = goAsync()
        context.appContainer.applicationScope.launch {
            try { ReminderScheduler.sync(context, context.appContainer.settings.settings.first(), replace = true) }
            finally { pending.finish() }
        }
    }
}
