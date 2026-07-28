package com.example.lsqrd.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.lsqrd.data.Credential
import com.example.lsqrd.data.Vault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialListScreen(
    viewModel: AppViewModel,
    vaultId: Long,
    onCredentialClick: (credentialId: Long) -> Unit,
    onBack: () -> Unit
) {
    val vault by produceState<Vault?>(initialValue = null, key1 = vaultId) {
        viewModel.getVaultById(vaultId).collect { value = it }
    }

    val credentials by produceState(initialValue = emptyList<Credential>(), key1 = vaultId) {
        viewModel.getCredentials(vaultId).collect { value = it }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var credentialToDelete by remember { mutableStateOf<Credential?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vault?.name ?: "Credentials") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add credential")
            }
        }
    ) { innerPadding ->
        if (credentials.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No credentials yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(credentials, key = { it.id }) { credential ->
                    CredentialRow(
                        credential = credential,
                        onClick = { onCredentialClick(credential.id) },
                        onDelete = { credentialToDelete = credential }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCredentialDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addCredential(vaultId, name)
                showAddDialog = false
            }
        )
    }

    credentialToDelete?.let { credential ->
        ConfirmDeleteDialog(
            title = "Delete credential",
            message = "Delete \"${credential.name}\"? All its fields will also be deleted.",
            onConfirm = {
                viewModel.deleteCredential(credential)
                credentialToDelete = null
            },
            onDismiss = { credentialToDelete = null }
        )
    }
}

@Composable
fun CredentialRow(
    credential: Credential, onClick: () -> Unit, onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(credential.name, fontWeight = FontWeight.Medium) },
        trailingContent = {
            IconButton(onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete credential")
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider()
}