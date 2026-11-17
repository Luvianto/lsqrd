package com.example.lsqrd.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lsqrd.data.Credential
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onCredentialClick: (credentialId: Long) -> Unit,
    onVaultView: (vaultId: Long) -> Unit
) {
    val vaults by viewModel.vaults.collectAsState()
    val selectedVaultId by viewModel.selectedVaultId.collectAsState()
    val allCredentials by viewModel.filteredCredentials.collectAsState()

    var showAddVaultDialog by remember { mutableStateOf(false) }
    var showAddCredentialDialog by remember { mutableStateOf(false) }
    var credentialToDelete by remember { mutableStateOf<Credential?>(null) }
    var credentialToEdit by remember { mutableStateOf<Credential?>(null) }
    var credentialToMove by remember { mutableStateOf<Credential?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortAscending by remember { mutableStateOf(true) }

    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportPassphrase by remember { mutableStateOf("") }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            viewModel.exportAll(
                passphrase = exportPassphrase,
                uri = uri,
                context = context,
                onSuccess = {
                    exportPassphrase = ""
                    Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    exportPassphrase = ""
                    Toast.makeText(context, "Export failed: $error", Toast.LENGTH_LONG).show()
                })
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportDialog = true
        }
    }

    val displayedCredentials = allCredentials.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }.let { list ->
        if (sortAscending) list.sortedBy { it.name.lowercase() }
        else list.sortedByDescending { it.name.lowercase() }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            if (!isSearchActive) {
                Text("lsqrd")
            } else {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search credentials..") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }, actions = {
            if (!isSearchActive) {
                TextButton(onClick = { sortAscending = !sortAscending }) {
                    Text(if (sortAscending) "A→Z" else "Z→A")
                }
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Export") }, onClick = {
                            showMenu = false
                            showExportDialog = true
                        })
                        DropdownMenuItem(text = { Text("Import") }, onClick = {
                            showMenu = false
                            importLauncher.launch(arrayOf("*/*"))
                        })
                    }
                }
            } else {
                IconButton(onClick = {
                    searchQuery = ""
                    isSearchActive = false
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close search")
                }
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showAddCredentialDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "Add Vault")
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Vault filter chips ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" chip
                FilterChip(
                    selected = selectedVaultId == null,
                    onClick = { viewModel.selectedVaultId.value = null },
                    label = { Text("All") })

                vaults.forEach { vault ->
                    val isSelected = selectedVaultId == vault.id
                    FilterChip(selected = isSelected, onClick = {
                        viewModel.selectedVaultId.value = if (isSelected) null else vault.id
                    }, label = { Text(vault.name) }, trailingIcon = {
                        if (isSelected) {
                            TextButton(
                                onClick = { onVaultView(vault.id) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    0.dp
                                )
                            ) {
                                Text("View", style = MaterialTheme.typography.labelSmall)
                            }
                        } else null
                    })
                }
                // "+" chip
                AssistChip(
                    onClick = { showAddVaultDialog = true },
                    label = { Text("+") },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }

            HorizontalDivider()

            if (displayedCredentials.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isBlank()) "No credentials yet. Tap + to add one."
                        else "No result for \"$searchQuery\""
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedCredentials, key = { it.id }) { credential ->
                        CredentialRow(
                            credential = credential,
                            onClick = { onCredentialClick(credential.id) },
                            onEdit = { credentialToEdit = credential },
                            onMove = { credentialToMove = credential },
                            onDelete = { credentialToDelete = credential }
                        )
                    }
                }
            }
        }
    }

    if (showAddVaultDialog) {
        AddVaultDialog(
            onDismiss = { showAddVaultDialog = false },
            onConfirm = { name ->
                viewModel.addVault(name)
                showAddVaultDialog = false
            }
        )
    }

    if (showAddCredentialDialog) {
        AddCredentialDialog(
            vaults = vaults,
            preselectedVaultId = selectedVaultId,
            onDismiss = { showAddCredentialDialog = false },
            onConfirm = { vaultId, name ->
                viewModel.addCredential(vaultId, name)
                showAddCredentialDialog = false
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

    credentialToEdit?.let { credential ->
        EditCredentialDialog(
            currentName = credential.name,
            onDismiss = { credentialToEdit = null },
            onConfirm = { newName ->
                viewModel.updateCredential(credential.copy(name = newName))
                credentialToEdit = null
            }
        )
    }

    credentialToMove?.let { credential ->
        MoveCredentialDialog (
            credentialName = credential.name,
            currentVaultId = credential.vaultId,
            vaults = vaults,
            onDismiss = { credentialToMove = null },
            onMove = { targetVault ->
                viewModel.moveCredential(credential, targetVault.id)
                credentialToMove = null
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(onDismiss = { showExportDialog = false }, onExport = { passphrase ->
            showExportDialog = false
            exportPassphrase = passphrase
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            exportLauncher.launch("lsqrd-backup-$date.lsqrd")
        })
    }

    pendingImportUri?.let { uri ->
        if (showImportDialog) {
            val fileName = uri.lastPathSegment ?: "backup file"
            ImportDialog(fileName = fileName, onDismiss = {
                showImportDialog = false
                pendingImportUri = null
            }, onImport = { passphrase ->
                showImportDialog = false
                viewModel.importAll(
                    passphrase = passphrase,
                    uri = uri,
                    context = context,
                    onSuccess = { result ->
                        pendingImportUri = null
                        Toast.makeText(
                            context,
                            "Imported ${result.vaultsImported} vault(s), ${result.credentialsImported} credential(s)",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onError = { error ->
                        pendingImportUri = null
                        Toast.makeText(context, "Import failed: $error", Toast.LENGTH_LONG).show()
                    })
            })
        }
    }
}

@Composable
fun CredentialRow(
    credential: Credential,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(credential.name, fontWeight = FontWeight.Medium) },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit credential")
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Move to vault") },
                            onClick = {
                                showMenu = false
                                onMove()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider()
}