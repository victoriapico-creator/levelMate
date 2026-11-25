package com.example.notesbysubject.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesbysubject.appContainer
import com.example.notesbysubject.data.sharing.ProjectFileCodec
import com.example.notesbysubject.data.sharing.ProjectFileException
import com.example.notesbysubject.data.sharing.ProjectTransferFiles
import com.example.notesbysubject.data.sharing.TransferProject
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharingViewModel(application: Application) : AndroidViewModel(application) {
    private val projects = application.appContainer.projects
    private val files = ProjectTransferFiles(application)
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var preview by mutableStateOf<TransferProject?>(null)
        private set
    var exportedFile by mutableStateOf<File?>(null)
        private set
    var importedId by mutableStateOf<Long?>(null)
        private set

    private fun perform(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        message = null
        viewModelScope.launch {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (invalid: ProjectFileException) { message = invalid.message }
            catch (_: SecurityException) { message = "File access was denied. Please choose the file again." }
            catch (_: IOException) { message = "Could not read or save the file. Check the available storage and try again." }
            catch (invalid: IllegalArgumentException) { message = invalid.message ?: "Invalid project data" }
            catch (_: Exception) { message = "Could not complete the file operation. Please try again." }
            finally { busy = false }
        }
    }

    private suspend fun projectText(id: Long): String = withContext(Dispatchers.IO) {
        val project = projects.dao.getProject(id) ?: throw ProjectFileException("Project no longer exists. Choose another project.")
        ProjectFileCodec.encode(project)
    }

    fun share(id: Long) = perform {
        val text = projectText(id)
        exportedFile = withContext(Dispatchers.IO) { files.prepareShare(text) }
        message = "Project file ready"
    }

    fun save(id: Long, uri: Uri) = perform {
        val text = projectText(id)
        withContext(Dispatchers.IO) { files.save(uri, text) }
        message = "Project file saved"
    }

    fun previewImport(uri: Uri) = perform {
        preview = null
        preview = withContext(Dispatchers.IO) { files.read(uri) }
    }

    fun importPreview() {
        val project = preview ?: return
        perform {
            val id = projects.createWithContents(
                project.name, project.description, project.deadline,
                project.materials.map { it.name to it.quantity },
                project.steps.sortedBy { it.position }.map { it.text to it.done }
            )
            preview = null
            importedId = id
        }
    }

    fun clearPreview() { if (!busy) preview = null }
    fun consumeExportedFile() { exportedFile = null }
    fun consumeImportedProject() { importedId = null }
    fun reportMessage(value: String) { message = value }
}
