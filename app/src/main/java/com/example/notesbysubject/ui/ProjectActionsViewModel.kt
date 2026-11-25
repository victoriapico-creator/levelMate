package com.example.notesbysubject.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesbysubject.appContainer
import com.example.notesbysubject.data.local.deadlineFromForm
import com.example.notesbysubject.data.templates.ProjectTemplate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ProjectActionsViewModel(application: Application) : AndroidViewModel(application) {
    val container = application.appContainer
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var savedProjectId by mutableStateOf<Long?>(null)
        private set
    var deleted by mutableStateOf(false)

    fun perform(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        message = null
        viewModelScope.launch {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (invalid: IllegalArgumentException) { message = invalid.message ?: "Invalid input" }
            catch (_: Exception) { message = "Could not save the change. Please try again." }
            finally { busy = false }
        }
    }

    fun consumeSavedProject() { savedProjectId = null }

    fun create(name: String, description: String, deadline: String) = perform {
        savedProjectId = container.projects.create(name, description, deadlineFromForm(deadline))
    }

    fun createFromTemplate(template: ProjectTemplate) = perform {
        template.validate()
        savedProjectId = container.projects.createWithContents(
            template.name, template.description, null,
            template.materials.map { it.name to it.quantity }, template.steps.map { it to false }
        )
    }
}
