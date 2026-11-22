package com.example.notesbysubject.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.levelMateSettings by preferencesDataStore(name = "levelmate_settings")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.levelMateSettings
    private val hour = intPreferencesKey("reminder_hour")
    private val minute = intPreferencesKey("reminder_minute")
    private val tolerance = floatPreferencesKey("level_tolerance")
    private val enabled = booleanPreferencesKey("reminders_enabled")

    val settings = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { values ->
        AppSettings(
            reminderHour = values[hour]?.takeIf { it in 0..23 } ?: 18,
            reminderMinute = values[minute]?.takeIf { it in 0..59 } ?: 0,
            levelTolerance = values[tolerance]?.takeIf { it.isFinite() && it in 0.1f..15f } ?: 1.5f,
            remindersEnabled = values[enabled] ?: false
        )
    }

    suspend fun save(value: AppSettings) {
        require(value.reminderHour in 0..23 && value.reminderMinute in 0..59) { "Invalid reminder time" }
        require(value.levelTolerance.isFinite() && value.levelTolerance in 0.1f..15f) { "Invalid level tolerance" }
        store.edit {
            it[hour] = value.reminderHour
            it[minute] = value.reminderMinute
            it[tolerance] = value.levelTolerance
            it[enabled] = value.remindersEnabled
        }
    }
}
