package com.example.notesbysubject

import android.app.Application
import android.content.Context
import com.example.notesbysubject.data.local.LevelMateDatabase
import com.example.notesbysubject.data.local.ProjectRepository
import com.example.notesbysubject.data.settings.SettingsRepository
import com.example.notesbysubject.data.templates.TemplateRepository
import com.example.notesbysubject.data.sharing.SharingRepository
import com.example.notesbysubject.reminders.ReminderScheduler
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LevelMateApplication : Application() {
    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        container.applicationScope.launch { container.importPhotos() }
        container.applicationScope.launch {
            try { ReminderScheduler.sync(this@LevelMateApplication, container.settings.settings.first()) }
            catch (_: Exception) { /* Settings can be saved again to retry scheduling. */ }
        }
    }
}

class AppContainer(context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val projects = ProjectRepository(LevelMateDatabase.getInstance(context))
    val settings = SettingsRepository(context)
    val templates = TemplateRepository.create(context.applicationContext)
    val sharing by lazy { SharingRepository(context.applicationContext) }
    val notesDirectory = File(context.filesDir, "notes")
    val photoImportError = MutableStateFlow<String?>(null)

    suspend fun importPhotos() {
        try {
            projects.importExistingPhotos(notesDirectory)
            photoImportError.value = null
        } catch (_: Exception) {
            photoImportError.value = "Some saved photos could not be loaded. Try loading photos again."
        }
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as LevelMateApplication).container
