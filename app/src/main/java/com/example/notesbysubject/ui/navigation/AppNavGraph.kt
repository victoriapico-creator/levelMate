package com.example.notesbysubject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notesbysubject.ui.screens.CameraScreen
import com.example.notesbysubject.ui.screens.ProjectDetailScreen
import com.example.notesbysubject.ui.screens.ProjectFormScreen
import com.example.notesbysubject.ui.screens.ProjectsScreen
import com.example.notesbysubject.ui.screens.SettingsScreen
import com.example.notesbysubject.ui.screens.SharedScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "projects",
        modifier = modifier
    ) {
        composable("projects") {
            ProjectsScreen(
                onNavigateToProjectForm = { navController.navigate("projectForm") },
                onNavigateToProjectDetail = { projectId -> navController.navigate("projectDetail/$projectId") },
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToShared = { navController.navigate("shared") }
            )
        }
        composable("projectForm") {
            ProjectFormScreen(onProjectSaved = { id ->
                navController.navigate("projectDetail/$id") {
                    popUpTo("projectForm") { inclusive = true }
                }
            })
        }
        composable("projectDetail/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            ProjectDetailScreen(
                projectId = projectId,
                onNavigateToCamera = { id -> navController.navigate("camera?projectId=$id") },
                onProjectDeleted = { navController.popBackStack() }
            )
        }
        composable(
            "camera?projectId={projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { entry ->
            CameraScreen(projectId = entry.arguments?.getString("projectId")?.toLongOrNull())
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("shared") {
            SharedScreen(onProjectImported = { id -> navController.navigate("projectDetail/$id") })
        }
    }
}
