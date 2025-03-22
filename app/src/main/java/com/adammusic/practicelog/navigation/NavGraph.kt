package com.adammusic.practicelog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adammusic.practicelog.ui.screens.home.HomeScreen
import com.adammusic.practicelog.ui.screens.practice.active.ActivePracticeScreen
import com.adammusic.practicelog.ui.screens.practice.editpractice.EditPracticeScreen
import com.adammusic.practicelog.ui.screens.practice.editsession.EditSessionScreen
import com.adammusic.practicelog.ui.screens.practice.newpractice.NewPracticeScreen
import com.adammusic.practicelog.ui.screens.practice.detail.PracticeDetailScreen
import com.adammusic.practicelog.ui.screens.settings.backup.BackupScreen
import com.adammusic.practicelog.ui.screens.settings.reminders.RemindersScreen
import com.adammusic.practicelog.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPracticeClick = { practiceId ->
                    navController.navigate(Screen.PracticeDetail.createRoute(practiceId))
                },
                onNewPracticeClick = {
                    navController.navigate(Screen.NewPractice.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.PracticeDetail.route) { backStackEntry ->
            val practiceId = backStackEntry.arguments?.getString("practiceId")?.toIntOrNull() ?: return@composable
            PracticeDetailScreen(
                practiceId = practiceId,
                onStartPractice = {
                    navController.navigate(Screen.ActivePractice.createRoute(practiceId))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onEditPractice = {
                    navController.navigate(Screen.EditPractice.createRoute(practiceId))
                },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.EditSession.createRoute(practiceId, sessionId))
                },
                onPracticeDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ActivePractice.route) { backStackEntry ->
            val practiceId = backStackEntry.arguments?.getString("practiceId")?.toIntOrNull() ?: return@composable
            ActivePracticeScreen(
                practiceId = practiceId,
                onBackClick = {
                    navController.popBackStack()
                },
                onSessionComplete = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditPractice.route) { backStackEntry ->
            val practiceId = backStackEntry.arguments?.getString("practiceId")?.toIntOrNull() ?: return@composable
            EditPracticeScreen(
                practiceId = practiceId,
                onPracticeUpdated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.EditSession.route) { backStackEntry ->
            val practiceId = backStackEntry.arguments?.getString("practiceId")?.toIntOrNull() ?: return@composable
            val sessionId = backStackEntry.arguments?.getString("sessionId")?.toIntOrNull() ?: return@composable
            EditSessionScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSessionUpdated = {
                    navController.popBackStack()
                },
                onSessionDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.NewPractice.route) {
            NewPracticeScreen(
                onPracticeCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToReminders = {
                    navController.navigate(Screen.Reminders.route)
                },
                onNavigateToBackup = {
                    navController.navigate(Screen.BackupRestore.route)
                }
            )
        }
        
        composable(Screen.Reminders.route) {
            RemindersScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.BackupRestore.route) {
            BackupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}