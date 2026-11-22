package com.example.notesbysubject.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesbysubject.appContainer
import com.example.notesbysubject.data.local.deadlineForForm
import com.example.notesbysubject.data.sharing.projectFileName
import com.example.notesbysubject.data.sharing.shareProjectFile
import com.example.notesbysubject.ui.SharingViewModel
import com.example.notesbysubject.ui.observeData

@Composable
fun SharedScreen(modifier: Modifier = Modifier, onProjectImported: (Long) -> Unit = {}) {
    val context = LocalContext.current
    val model: SharingViewModel = viewModel()
    var retry by remember { mutableStateOf(0) }
    val localProjects by observeData(context.appContainer.projects.projects, retry)
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingSaveId by rememberSaveable { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val selected = localProjects.data.orEmpty().firstOrNull { it.id == selectedId } ?: localProjects.data.orEmpty().firstOrNull()

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val id = pendingSaveId
        pendingSaveId = null
        if (uri != null && id != null) model.save(id, uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) model.previewImport(uri)
    }
    LaunchedEffect(model.importedId) {
        model.importedId?.let { model.consumeImportedProject(); onProjectImported(it) }
    }
    LaunchedEffect(model.exportedFile) {
        model.exportedFile?.let { file ->
            model.consumeExportedFile()
            try { shareProjectFile(context, file) }
            catch (_: Exception) { model.reportMessage("Could not open a sharing app. Use Save project file instead.") }
        }
    }

    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Share and import projects", style = MaterialTheme.typography.headlineMedium)
            Text("Save or send a project file containing its materials and steps. The other person can import it as a new project. Photos are shared separately from the project detail.")
            model.message?.let { Text(it) }
            if (model.busy) CircularProgressIndicator()
            Text("Export a project", style = MaterialTheme.typography.titleLarge)
            if (localProjects.loading) CircularProgressIndicator()
            localProjects.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
                Button(onClick = { retry++ }) { Text("Retry") }
            }
            if (!localProjects.loading && localProjects.error == null && localProjects.data.isNullOrEmpty()) {
                Text("Create a project first to export it. You can still import a file below.")
            }
            Box {
                OutlinedButton(enabled = selected != null && !model.busy, onClick = { expanded = true }) {
                    Text(selected?.name ?: "No local projects")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    localProjects.data.orEmpty().forEach { project ->
                        DropdownMenuItem(text = { Text(project.name) }, onClick = { selectedId = project.id; expanded = false })
                    }
                }
            }
            Button(enabled = selected != null && !model.busy, onClick = { selected?.let { model.share(it.id) } }) {
                Text("Share project file")
            }
            OutlinedButton(enabled = selected != null && !model.busy && pendingSaveId == null, onClick = {
                selected?.let { project ->
                    pendingSaveId = project.id
                    try { saveLauncher.launch(projectFileName(project.name)) }
                    catch (_: Exception) { pendingSaveId = null; model.reportMessage("Could not open the file picker.") }
                }
            }) { Text("Save project file") }
            Text("Import a project", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(enabled = !model.busy, onClick = {
                try { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }
                catch (_: Exception) { model.reportMessage("Could not open the file picker.") }
            }) { Text("Choose project file") }
            Text("Choose a LevelMate .json file up to 256 KB. You will review it before saving a new copy.")
        }
        model.preview?.let { project ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Review imported project", style = MaterialTheme.typography.titleLarge)
                        Text(project.name, style = MaterialTheme.typography.titleMedium)
                        if (project.description.isNotBlank()) Text(project.description)
                        Text(project.deadline?.let { "Due ${deadlineForForm(it)}" } ?: "No deadline")
                        Text("Materials", style = MaterialTheme.typography.titleSmall)
                        if (project.materials.isEmpty()) Text("No materials")
                        project.materials.forEach { Text("${it.name} · ${it.quantity}") }
                        Text("Steps", style = MaterialTheme.typography.titleSmall)
                        if (project.steps.isEmpty()) Text("No steps")
                        project.steps.forEachIndexed { index, step -> Text("${index + 1}. ${step.text}${if (step.done) " (done)" else ""}") }
                        Button(enabled = !model.busy, onClick = model::importPreview) { Text("Import as a new project") }
                        TextButton(enabled = !model.busy, onClick = model::clearPreview) { Text("Cancel import") }
                    }
                }
            }
        }
    }
}

@Composable
fun Shared(modifier: Modifier = Modifier) = SharedScreen(modifier)
