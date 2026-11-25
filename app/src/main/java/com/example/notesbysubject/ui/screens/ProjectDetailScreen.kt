package com.example.notesbysubject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.notesbysubject.data.local.Project
import com.example.notesbysubject.data.local.deadlineForForm
import com.example.notesbysubject.data.local.deadlineFromForm
import com.example.notesbysubject.ui.ProjectActionsViewModel
import com.example.notesbysubject.ui.components.ProjectFormFields
import com.example.notesbysubject.ui.observeData
import com.example.notesbysubject.ui.photos.PhotoCard
import kotlinx.coroutines.flow.flowOf

@Composable
fun ProjectDetailScreen(
    modifier: Modifier = Modifier,
    projectId: String? = null,
    onNavigateToCamera: (Long) -> Unit = {},
    onProjectDeleted: () -> Unit = {}
) {
    val id = projectId?.toLongOrNull()?.takeIf { it > 0 }
    val container = LocalContext.current.appContainer
    val actions: ProjectActionsViewModel = viewModel()
    var retry by remember { mutableStateOf(0) }
    val projectFlow = remember(id) { if (id == null) flowOf(null) else container.projects.dao.observeProject(id) }
    val state by observeData(projectFlow, retry)
    val generalPhotos by observeData(container.projects.generalPhotos, retry)
    var materialName by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var stepText by rememberSaveable { mutableStateOf("") }
    var showGeneral by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(actions.deleted) { if (actions.deleted) { actions.deleted = false; onProjectDeleted() } }

    val detail = state.data
    if (state.loading) { CircularProgressIndicator(Modifier.padding(24.dp)); return }
    if (state.error != null) {
        Column(modifier.padding(16.dp)) { Text(state.error!!); Button(onClick = { retry++ }) { Text("Retry") } }
        return
    }
    if (detail == null) { Text("Project not found. It may have been deleted.", modifier.padding(24.dp)); return }
    val project = detail.project
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(project.name, style = MaterialTheme.typography.headlineMedium)
            if (project.description.isNotBlank()) Text(project.description)
            Text(project.deadline?.let { "Due ${deadlineForForm(it)}" } ?: "No deadline")
            actions.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedButton(onClick = { editing = !editing }) { Text(if (editing) "Cancel editing" else "Edit project") }
            if (editing) ProjectEditor(project, actions) { editing = false }
            Text("Materials", style = MaterialTheme.typography.titleLarge)
            if (detail.materials.isEmpty()) Text("No materials added.")
        }
        items(detail.materials.sortedBy { it.id }, key = { "material-${it.id}" }) { material ->
            Column {
                Text("${material.name} · ${material.quantity}")
                TextButton(enabled = !actions.busy, onClick = { actions.perform { container.projects.dao.deleteMaterial(material.id) } }) { Text("Remove material") }
            }
        }
        item {
            OutlinedTextField(materialName, { materialName = it }, label = { Text("Material name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(quantity, { quantity = it }, label = { Text("Quantity, including units") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(enabled = !actions.busy && materialName.isNotBlank() && materialName.length <= 80 && quantity.isNotBlank() && quantity.length <= 40, onClick = {
                actions.perform { container.projects.addMaterial(project.id, materialName, quantity); materialName = ""; quantity = "" }
            }) { Text("Add material") }
            Text("Steps", style = MaterialTheme.typography.titleLarge)
            if (detail.steps.isEmpty()) Text("No steps added.")
        }
        items(detail.steps.sortedWith(compareBy({ it.position }, { it.id })), key = { "step-${it.id}" }) { step ->
            Column {
                Row {
                    Checkbox(checked = step.done, enabled = !actions.busy, onCheckedChange = { checked -> actions.perform { container.projects.dao.setStepDone(step.id, checked) } })
                    Text(step.text, modifier = Modifier.padding(top = 12.dp))
                }
                TextButton(enabled = !actions.busy, onClick = { actions.perform { container.projects.dao.deleteStep(step.id) } }) { Text("Remove step") }
            }
        }
        item {
            OutlinedTextField(stepText, { stepText = it }, label = { Text("Next step") }, modifier = Modifier.fillMaxWidth(), supportingText = { Text("Up to 300 characters") })
            Button(enabled = !actions.busy && stepText.isNotBlank() && stepText.length <= 300, onClick = {
                actions.perform { container.projects.addStep(project.id, stepText); stepText = "" }
            }) { Text("Add step") }
            Text("Photos", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { onNavigateToCamera(project.id) }) { Text("Take project photo") }
            if (detail.photos.isEmpty()) Text("No project photos yet.")
        }
        items(detail.photos.sortedByDescending { it.takenAt }, key = { "photo-${it.id}" }) { PhotoCard(it) }
        item { OutlinedButton(onClick = { showGeneral = !showGeneral }) { Text(if (showGeneral) "Hide general photos" else "Attach a saved photo") } }
        if (showGeneral) {
            item {
                generalPhotos.error?.let { Text(it); Button(onClick = { retry++ }) { Text("Retry") } }
                if (!generalPhotos.loading && generalPhotos.error == null && generalPhotos.data.isNullOrEmpty()) Text("No general photos available.")
            }
            items(generalPhotos.data.orEmpty(), key = { "general-${it.id}" }) { photo ->
                PhotoCard(photo, onAssign = { actions.perform { container.projects.dao.assignPhoto(photo.id, project.id) } }, enabled = !actions.busy)
            }
        }
        item { TextButton(enabled = !actions.busy, onClick = { confirmDelete = true }) { Text("Delete project") } }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete project?") },
        text = { Text("Materials and steps will be removed. Photos will stay in general photos.") },
        confirmButton = { TextButton(onClick = {
            confirmDelete = false
            actions.perform { container.projects.dao.deleteProject(project.id); actions.deleted = true }
        }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
    )
}

@Composable
private fun ProjectEditor(project: Project, actions: ProjectActionsViewModel, onSaved: () -> Unit) {
    var name by rememberSaveable(project.id) { mutableStateOf(project.name) }
    var description by rememberSaveable(project.id) { mutableStateOf(project.description) }
    var deadline by rememberSaveable(project.id) { mutableStateOf(deadlineForForm(project.deadline)) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProjectFormFields(name, { name = it }, validateName(name), description, { description = it }, validateDescription(description), deadline, { deadline = it }, validateDeadline(deadline))
        Button(enabled = !actions.busy && validateName(name) == null && validateDescription(description) == null && validateDeadline(deadline) == null, onClick = {
            actions.perform { actions.container.projects.update(project.copy(name = name, description = description, deadline = deadlineFromForm(deadline))); onSaved() }
        }) { Text("Save changes") }
    }
}

@Composable
fun ProjectDetail(modifier: Modifier = Modifier, projectId: String? = null) = ProjectDetailScreen(modifier, projectId)
