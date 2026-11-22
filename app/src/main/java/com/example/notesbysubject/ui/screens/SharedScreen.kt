package com.example.notesbysubject.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesbysubject.appContainer
import com.example.notesbysubject.data.local.deadlineForForm
import com.example.notesbysubject.data.sharing.SharedProject
import com.example.notesbysubject.data.sharing.SharedListState
import com.example.notesbysubject.ui.SharingViewModel
import com.example.notesbysubject.ui.observeData

@Composable
fun SharedScreen(modifier: Modifier = Modifier, onProjectImported: (Long) -> Unit = {}) {
    val context = LocalContext.current
    val model: SharingViewModel = viewModel()
    val user by model.user.collectAsState()
    val observedShared by model.sharedProjects.collectAsState()
    val shared = if (observedShared.viewerUid == user?.uid) observedShared else SharedListState(loading = user != null)
    val localProjects by observeData(context.appContainer.projects.projects)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var recipient by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var deleteProject by remember { mutableStateOf<SharedProject?>(null) }
    val selected = localProjects.data.orEmpty().firstOrNull { it.id == selectedId } ?: localProjects.data.orEmpty().firstOrNull()
    LaunchedEffect(model.importedId) {
        model.importedId?.let { model.consumeImportedProject(); onProjectImported(it) }
    }
    LaunchedEffect(user?.uid) { password = ""; recipient = "" }

    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Shared projects", style = MaterialTheme.typography.headlineMedium)
            model.message?.let { Text(it) }
            if (!model.configured) {
                Text("Project sharing is not available yet. Contact the project owner to finish setting it up.")
            } else if (user == null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sign in to share project plans. Photos stay on your phone.")
                    OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    val valid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() && password.length >= 6 && !model.busy
                    Button(enabled = valid, onClick = {
                        val entered = password; password = ""
                        model.authenticate(email.trim(), entered, false)
                    }) { Text(if (model.busy) "Please wait…" else "Sign in") }
                    OutlinedButton(enabled = valid, onClick = {
                        val entered = password; password = ""
                        model.authenticate(email.trim(), entered, true)
                    }) { Text("Create account") }
                    Text("Use at least 6 characters for a new password.")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Signed in as ${user?.email.orEmpty()}")
                    Text("Your UID: ${user?.uid.orEmpty()}")
                    OutlinedButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("LevelMate UID", user?.uid.orEmpty()))
                    }) { Text("Copy your UID") }
                    TextButton(onClick = model::logout, enabled = !model.busy) { Text("Sign out") }
                    Text("Share a local project", style = MaterialTheme.typography.titleLarge)
                    localProjects.error?.let { Text(it) }
                    if (localProjects.data.isNullOrEmpty()) Text("Create a project first to share it.")
                    Box {
                        OutlinedButton(enabled = !localProjects.data.isNullOrEmpty(), onClick = { expanded = true }) {
                            Text(selected?.name ?: "No local projects")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            localProjects.data.orEmpty().forEach { project ->
                                DropdownMenuItem(text = { Text(project.name) }, onClick = { selectedId = project.id; expanded = false })
                            }
                        }
                    }
                    OutlinedTextField(recipient, { recipient = it }, label = { Text("Other person's UID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Ask the other person to sign in and copy their UID. Each project supports up to 10 participants.")
                    Button(enabled = selected != null && recipient.isNotBlank() && recipient.trim() != user?.uid && recipient.trim().length <= 128 && !model.busy, onClick = {
                        selected?.let { model.publish(it.id, recipient) }
                    }) { Text("Share / update shared copy") }
                    Text("Recipients can read this snapshot or save their own local copy. Share it again to publish later changes. Photos are excluded.")
                    OutlinedButton(onClick = model::retry, enabled = !model.busy) { Text("Refresh shared projects") }
                    if (shared.loading) CircularProgressIndicator()
                    shared.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (shared.fromCache) Text("Showing cached projects. Connect to the internet to sync changes.")
                    if (!shared.loading && shared.projects.isEmpty()) Text("No shared projects yet.")
                }
            }
        }
        if (user != null) items(shared.projects, key = { it.id }) { project ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium)
                    Text(if (project.ownerUid == user?.uid) "You own this shared copy" else "Shared with you")
                    if (project.description.isNotBlank()) Text(project.description)
                    Text(project.deadline?.let { "Due ${deadlineForForm(it)}" } ?: "No deadline")
                    Text("Materials", style = MaterialTheme.typography.titleSmall)
                    if (project.materials.isEmpty()) Text("No materials")
                    project.materials.forEach { Text("${it.name} · ${it.quantity}") }
                    Text("Steps", style = MaterialTheme.typography.titleSmall)
                    if (project.steps.isEmpty()) Text("No steps")
                    project.steps.forEachIndexed { index, step -> Text("${index + 1}. ${step.text}${if (step.done) " (done)" else ""}") }
                    Button(enabled = !model.busy, onClick = { model.import(project) }) { Text("Save as a local project") }
                    if (project.ownerUid == user?.uid) {
                        TextButton(enabled = !model.busy, onClick = { deleteProject = project }) { Text("Stop sharing") }
                    }
                }
            }
        }
    }
    deleteProject?.let { project ->
        AlertDialog(
            onDismissRequest = { deleteProject = null },
            title = { Text("Stop sharing this project?") },
            text = { Text("Participants will lose access to this shared copy. Saved local copies will remain.") },
            confirmButton = { TextButton(onClick = { deleteProject = null; model.remove(project) }) { Text("Stop sharing") } },
            dismissButton = { TextButton(onClick = { deleteProject = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun Shared(modifier: Modifier = Modifier) = SharedScreen(modifier)
