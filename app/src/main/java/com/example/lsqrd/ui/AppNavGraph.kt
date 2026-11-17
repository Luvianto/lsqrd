package com.example.lsqrd.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavGraph(appViewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            HomeScreen (
                viewModel = appViewModel,
                onCredentialClick = { credentialId ->
                    navController.navigate("credentials/$credentialId/fields")
                },
                onVaultView = {vaultId ->
                    navController.navigate("vaults/$vaultId")
                }
            )
        }
        composable(
            "vaults/{vaultId}",
            arguments = listOf(navArgument("vaultId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vaultId = backStackEntry.arguments!!.getLong("vaultId")
            VaultDetailScreen (
                viewModel = appViewModel,
                vaultId = vaultId,
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }
        composable(
            "credentials/{credentialId}/fields",
            arguments = listOf(navArgument("credentialId") { type = NavType.LongType })
        ) { backStackEntry ->
            val credentialId = backStackEntry.arguments!!.getLong("credentialId")
            CredentialDetailScreen(
                viewModel = appViewModel,
                credentialId = credentialId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}