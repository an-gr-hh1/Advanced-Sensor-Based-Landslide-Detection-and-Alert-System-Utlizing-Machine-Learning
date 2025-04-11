package com.example.advancedlandslideapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.advancedlandslideapp.screens.*

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegistrationScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("map") { MapScreen(navController) }
        composable("forum") { CommunityForumScreen(navController) }
        composable("report") { ReportIncidentScreen(navController) }
        composable("disaster") { DisasterPreparednessScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}