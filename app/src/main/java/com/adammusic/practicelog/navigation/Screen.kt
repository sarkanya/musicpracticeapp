package com.adammusic.practicelog.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PracticeDetail : Screen("practice/{practiceId}") {
        fun createRoute(practiceId: Int) = "practice/$practiceId"
    }
    object ActivePractice : Screen("practice/{practiceId}/session") {
        fun createRoute(practiceId: Int) = "practice/$practiceId/session"
    }
    object EditPractice : Screen("practice/{practiceId}/edit") {
        fun createRoute(practiceId: Int) = "practice/$practiceId/edit"
    }
    object EditSession : Screen("practice/{practiceId}/session/{sessionId}/edit") {
        fun createRoute(practiceId: Int, sessionId: Int) = "practice/$practiceId/session/$sessionId/edit"
    }
    object NewPractice : Screen("practice/new")
    object Settings : Screen("settings")
    object Reminders : Screen("reminders")
    object BackupRestore : Screen("backup")
}
