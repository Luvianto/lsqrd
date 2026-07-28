package com.example.lsqrd.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
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
        startDestination = "vaults",
    ) {
        composable("vaults") {
            VaultListScreen(
                viewModel = appViewModel,
                onVaultClick = { vaultId ->
                    navController.navigate("credentials/$vaultId")
                }
            )
        }
        composable(
            "crendentials/{vaultId}",
            arguments = listOf(navArgument("vaultId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vaultId = backStackEntry.arguments!!.getLong("vaultId")
            CredentialListScreen(
                viewModel = appViewModel,
                vaultId = vaultId,
                onCredentialClick = { credentialId ->
                    navController.navigate("field/$credentialId")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "fields/{credentialId}",
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