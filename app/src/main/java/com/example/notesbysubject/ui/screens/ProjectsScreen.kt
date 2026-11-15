package com.example.notesbysubject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProjectsScreen(
    onNavigateToProjectForm: () -> Unit,
    onNavigateToProjectDetail: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShared: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Projects",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            onClick = onNavigateToProjectForm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ProjectForm")
        }
        Button(
            onClick = { onNavigateToProjectDetail("sampleId") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ProjectDetail")
        }
        Button(
            onClick = onNavigateToCamera,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Camera")
        }
        Button(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }
        Button(
            onClick = onNavigateToShared,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Shared")
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
) {
    ProjectsScreen(
        onNavigateToProjectForm = onNavigateToProjectForm,
        onNavigateToProjectDetail = onNavigateToProjectDetail,
        onNavigateToCamera = onNavigateToCamera,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToShared = onNavigateToShared,
        modifier = modifier
    )
}
