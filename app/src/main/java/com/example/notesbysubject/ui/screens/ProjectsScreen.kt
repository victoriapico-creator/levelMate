package com.example.notesbysubject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesbysubject.appContainer
import com.example.notesbysubject.data.local.deadlineForForm
import com.example.notesbysubject.data.templates.TemplateResult
import com.example.notesbysubject.data.templates.TemplateSource
import com.example.notesbysubject.ui.ProjectActionsViewModel
import com.example.notesbysubject.ui.observeData
import com.example.notesbysubject.ui.photos.PhotoCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ProjectsScreen(
    onNavigateToProjectForm: () -> Unit,
    onNavigateToProjectDetail: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShared: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = LocalContext.current.appContainer
    val actions: ProjectActionsViewModel = viewModel()
    var retry by remember { mutableStateOf(0) }
    val projects by observeData(container.projects.projects, retry)
    val photos by observeData(container.projects.generalPhotos, retry)
    val importError by container.photoImportError.collectAsState()
    val scope = rememberCoroutineScope()
    var showPhotos by rememberSaveable { mutableStateOf(false) }
    var catalog by remember { mutableStateOf<TemplateResult?>(null) }
    var loadingTemplates by remember { mutableStateOf(false) }
    var templateError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actions.savedProjectId) {
        actions.savedProjectId?.let {
            actions.consumeSavedProject()
            onNavigateToProjectDetail(it.toString())
        }
    }
    LazyColumn(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LevelMate", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onNavigateToProjectForm, modifier = Modifier.fillMaxWidth()) { Text("New project") }
                OutlinedButton(onClick = onNavigateToCamera, modifier = Modifier.fillMaxWidth()) { Text("Camera") }
                OutlinedButton(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
                OutlinedButton(onClick = onNavigateToShared, modifier = Modifier.fillMaxWidth()) { Text("Share / import projects") }
                actions.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Text("Projects", style = MaterialTheme.typography.titleLarge)
            }
        }
        if (projects.loading) item { CircularProgressIndicator() }
        projects.error?.let { error -> item {
            Text(error)
            Button(onClick = { retry++ }) { Text("Retry") }
        } }
        if (!projects.loading && projects.error == null && projects.data.isNullOrEmpty()) {
            item { Text("No projects yet. Create one or choose a template below.") }
        }
        items(projects.data.orEmpty(), key = { it.id }) { project ->
            Card(onClick = { onNavigateToProjectDetail(project.id.toString()) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium)
                    if (project.description.isNotBlank()) Text(project.description)
                    Text(project.deadline?.let { "Due ${deadlineForForm(it)}" } ?: "No deadline")
                }
            }
        }
        item {
            Text("Project templates", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(enabled = !loadingTemplates, onClick = {
                loadingTemplates = true
                templateError = null
                scope.launch {
                    try { catalog = container.templates.load() }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { templateError = "Could not load templates. Please retry." }
                    finally { loadingTemplates = false }
                }
            }) { Text(if (loadingTemplates) "Loading…" else "Load templates") }
            templateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            catalog?.let { result ->
                Text(when (result.source) {
                    TemplateSource.RECORDED -> "Built-in templates"
                    TemplateSource.REMOTE -> "Templates loaded from the internet"
                    TemplateSource.RECORDED_FALLBACK -> "Online templates unavailable. Showing the built-in copy."
                })
                if (result.catalog.templates.isEmpty()) Text("No templates available.")
            }
        }
        items(catalog?.catalog?.templates.orEmpty(), key = { "template-${it.id}" }) { template ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium)
                    Text(template.description)
                    Button(onClick = { actions.createFromTemplate(template) }, enabled = !actions.busy) { Text("Use template") }
                }
            }
        }
        item {
            OutlinedButton(onClick = { showPhotos = !showPhotos }) { Text(if (showPhotos) "Hide general photos" else "General photos") }
        }
        if (showPhotos) {
            item {
                OutlinedButton(onClick = { actions.perform { container.importPhotos() } }, enabled = !actions.busy) { Text("Reload saved photos") }
                importError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                photos.error?.let { Text(it); Button(onClick = { retry++ }) { Text("Retry") } }
                if (!photos.loading && photos.error == null && photos.data.isNullOrEmpty()) Text("No general photos. Take one with the camera.")
            }
            items(photos.data.orEmpty(), key = { "photo-${it.id}" }) { PhotoCard(it) }
        }
    }
}

@Composable
fun Projects(
    onNavigateToProjectForm: () -> Unit,
    onNavigateToProjectDetail: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShared: () -> Unit,
    modifier: Modifier = Modifier
) = ProjectsScreen(onNavigateToProjectForm, onNavigateToProjectDetail, onNavigateToCamera, onNavigateToSettings, onNavigateToShared, modifier)
