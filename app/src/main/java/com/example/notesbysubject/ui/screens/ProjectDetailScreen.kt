package com.example.notesbysubject.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProjectDetailScreen(
    modifier: Modifier = Modifier,
    projectId: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ProjectDetail",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ProjectDetail(
    modifier: Modifier = Modifier,
    projectId: String? = null
) {
    ProjectDetailScreen(
        modifier = modifier,
        projectId = projectId
    )
}
