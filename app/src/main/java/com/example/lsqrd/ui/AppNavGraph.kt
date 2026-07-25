package com.example.lsqrd.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
    }
}