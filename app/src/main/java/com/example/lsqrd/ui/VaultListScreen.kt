package com.example.lsqrd.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.lsqrd.data.Vault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    viewModel: AppViewModel,
    onVaultClick: (vaultId: Long) -> Unit
) {
    val vaults by viewModel.vaults.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lsqrd") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Vault")
            }
        }
    ) { innerPadding ->
        if (vaults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No vaults yet. Tap + to create one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(vaults, key = { it.id }) { vault ->
                    VaultRow(
                        vault = vault,
                        onClick = { onVaultClick(vault.id) },
                        onDelete = { viewModel.deleteVault(vault) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddVaultDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addVault(name)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun VaultRow(vault: Vault, onClick: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(vault.name, fontWeight = FontWeight.Medium) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Vault")
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider()
}