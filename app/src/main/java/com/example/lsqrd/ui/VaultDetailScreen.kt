package com.example.lsqrd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lsqrd.data.Vault
import com.example.lsqrd.data.VaultDefaultCredentialField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDetailScreen(
    viewModel: AppViewModel,
    vaultId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val vault by produceState<Vault?>(initialValue = null, key1 = vaultId) {
        viewModel.getVaultById(vaultId).collect { value = it }
    }
    val templateFields by produceState(emptyList<VaultDefaultCredentialField>(), vaultId) {
        viewModel.getTemplateFields(vaultId).collect { value = it }
    }

    var vaultName by remember(vault) { mutableStateOf(vault?.name ?: "") }
    var showDeleteVaultDialog by remember { mutableStateOf(false) }
    var showAddFieldDialog by remember { mutableStateOf(false) }
    var fieldToDelete by remember { mutableStateOf<VaultDefaultCredentialField?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vault?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteVaultDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete vault",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Vault Name", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = vaultName,
                    onValueChange = { vaultName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        vault?.let { viewModel.updateVault(it.copy(name = vaultName.trim())) }
                    },
                    enabled = vaultName.isNotBlank() && vaultName.trim() != vault?.name
                ) {
                    Text("Save name")
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Default Credential Fields", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { showAddFieldDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Add field")
                    }
                }
            }

            if (templateFields.isEmpty()) {
                item {
                    Text(
                        "No default fields yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(templateFields, key = { it.id }) { field ->
                    ListItem(
                        headlineContent = { Text(field.label) },
                        supportingContent = {
                            Text(if (field.isSecret) "Secret" else field.value.ifBlank { "No default value" })
                        },
                        trailingContent = {
                            IconButton(onClick = { fieldToDelete = field }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete field",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showAddFieldDialog) {
        AddVaultDefaultCredentialFieldDialog(
            onConfirm = { label, value, isSecret ->
                viewModel.addTemplateField(vaultId, label, value, isSecret)
            },
            onDismiss = { showAddFieldDialog = false },
        )
    }

    fieldToDelete?.let { field ->
        ConfirmDeleteDialog(
            title = "Delete field",
            message = "Remove \"${field.label}\" from default fields?",
            onConfirm = {
                viewModel.deleteTemplateField(field)
                fieldToDelete = null
            },
            onDismiss = { fieldToDelete = null }
        )
    }

    if (showDeleteVaultDialog) {
        ConfirmDeleteDialog(
            title = "Delete vault",
            message = "Delete \"${vaultName}\"? This will also delete all its credentials and fields",
            onConfirm = {
                vault?.let {
                    viewModel.selectedVaultId.value = null
                    viewModel.deleteVault(it)
                }
                onDeleted()
            },
            onDismiss = { showDeleteVaultDialog = false }
        )
    }
}