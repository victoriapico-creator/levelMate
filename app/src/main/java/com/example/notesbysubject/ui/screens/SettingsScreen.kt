package com.example.notesbysubject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.Switch
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.remember
import com.example.notesbysubject.reminders.ReminderScheduler
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.notesbysubject.appContainer
import com.example.notesbysubject.data.settings.AppSettings
import com.example.notesbysubject.data.settings.parseLevelTolerance
import com.example.notesbysubject.data.settings.parseReminderTime
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val container = LocalContext.current.appContainer
    val settings by container.settings.settings.collectAsState(initial = null)
    if (settings == null) {
        CircularProgressIndicator(modifier = modifier.padding(24.dp))
    } else {
        SettingsEditor(settings!!, modifier)
    }
}

@Composable
private fun SettingsEditor(settings: AppSettings, modifier: Modifier) {
    val context = LocalContext.current
    val container = context.appContainer
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationsAllowed by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var time by rememberSaveable(settings.reminderHour, settings.reminderMinute) {
        mutableStateOf(String.format(Locale.ROOT, "%02d:%02d", settings.reminderHour, settings.reminderMinute))
    }
    var tolerance by rememberSaveable(settings.levelTolerance) { mutableStateOf(settings.levelTolerance.toString()) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var remindersEnabled by rememberSaveable(settings.remindersEnabled) { mutableStateOf(settings.remindersEnabled) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        remindersEnabled = granted
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!granted) message = "Notifications were denied. You can enable them in system settings."
    }
    val parsedTime = parseReminderTime(time)
    val parsedTolerance = parseLevelTolerance(tolerance)

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = time, onValueChange = { time = it }, label = { Text("Reminder time (HH:mm)") },
            isError = parsedTime == null, singleLine = true, modifier = Modifier.fillMaxWidth(),
            supportingText = { if (parsedTime == null) Text("Enter a time from 00:00 to 23:59") }
        )
        OutlinedTextField(
            value = tolerance, onValueChange = { tolerance = it }, label = { Text("Level tolerance (degrees)") },
            isError = parsedTolerance == null, singleLine = true, modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Choose a value between 0.1 and 15 degrees") }
        )
        Text("Daily deadline reminder")
        Switch(checked = remindersEnabled, onCheckedChange = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else { remindersEnabled = enabled }
        })
        Text("Android chooses the execution time, so reminders may arrive late. Only unfinished projects due today or earlier are included.")
        if (!notificationsAllowed) {
            Text("Notifications are disabled in system settings.")
            OutlinedButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            }) { Text("Open notification settings") }
        }
        Button(
            enabled = parsedTime != null && parsedTolerance != null && !saving,
            onClick = {
                saving = true
                scope.launch {
                    try {
                        val updated = settings.copy(
                            reminderHour = parsedTime!!.first, reminderMinute = parsedTime.second,
                            levelTolerance = parsedTolerance!!, remindersEnabled = remindersEnabled
                        )
                        container.settings.save(updated)
                        ReminderScheduler.sync(context, updated, replace = true)
                        message = "Settings saved"
                    } catch (_: Exception) { message = "Could not save settings. Please try again." }
                    finally { saving = false }
                }
            }
        ) { Text(if (saving) "Saving…" else "Save settings") }
        message?.let { Text(it) }
    }
}

@Composable
fun Settings(modifier: Modifier = Modifier) = SettingsScreen(modifier)
