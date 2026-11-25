package com.example.notesbysubject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesbysubject.ui.ProjectActionsViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notesbysubject.ui.components.ProjectFormFields

@Composable
fun ProjectFormScreen(
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit = {},
    onProjectSaved: (Long) -> Unit = {}
) {
    val actions: ProjectActionsViewModel = viewModel()
    LaunchedEffect(actions.savedProjectId) {
        actions.savedProjectId?.let {
            actions.consumeSavedProject()
            onSaveClick()
            onProjectSaved(it)
        }
    }
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var deadline by rememberSaveable { mutableStateOf("") }

    val nameError = validateName(name)
    val descriptionError = validateDescription(description)
    val deadlineError = validateDeadline(deadline)

    val isFormValid = nameError == null && descriptionError == null && deadlineError == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "New project",
            style = MaterialTheme.typography.headlineMedium
        )

        ProjectFormFields(
            name = name,
            onNameChange = { name = it },
            nameError = nameError,
            description = description,
            onDescriptionChange = { description = it },
            descriptionError = descriptionError,
            deadline = deadline,
            onDeadlineChange = { deadline = it },
            deadlineError = deadlineError,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { actions.create(name, description, deadline) },
            enabled = isFormValid && !actions.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (actions.busy) "Saving…" else "Save")
        }
        actions.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
fun ProjectForm(
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit = {}
) {
    ProjectFormScreen(
        modifier = modifier,
        onSaveClick = onSaveClick
    )
}
