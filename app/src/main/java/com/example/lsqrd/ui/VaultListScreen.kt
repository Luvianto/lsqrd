package com.example.lsqrd.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lsqrd.data.Vault
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    viewModel: AppViewModel,
    onVaultClick: (vaultId: Long) -> Unit
) {
    val vaults by viewModel.vaults.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var vaultToDelete by remember { mutableStateOf<Vault?>(null) }
    var vaultToEdit by remember { mutableStateOf<Vault?>(null) }
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
        if(uri != null){
            viewModel.exportAll(
                passphrase = exportPassphrase,
                uri = uri,
                context = context,
                onSuccess = {
                    exportPassphrase = ""
                    Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
                },
                onError = {error ->
                    exportPassphrase = ""
                    Toast.makeText(context, "Export failed: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null){
            pendingImportUri = uri
            showImportDialog = true
        }
    }

    val sortedVaults = if (sortAscending) {
        vaults.sortedBy { it.name.lowercase() }
    } else {
        vaults.sortedByDescending { it.name.lowercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("lsqrd") },
                actions = {
                    TextButton(onClick = { sortAscending = !sortAscending }) {
                        Text(if (sortAscending) "A→Z" else "Z→A")
                    }
                    Box{
                        IconButton(onClick = {showMenu = true}) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = {showMenu = false}
                        ) {
                            DropdownMenuItem(
                                text = {Text("Export")},
                                onClick = {
                                    showMenu = false
                                    showExportDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {Text("Import")},
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("*/*"))
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Vault")
            }
        }
    ) { innerPadding ->
        if (sortedVaults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "No vaults yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap + to create your first vault",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(sortedVaults, key = { it.id }) { vault ->
                    VaultRow(
                        vault = vault,
                        onClick = { onVaultClick(vault.id) },
                        onEdit = { vaultToEdit = vault },
                        onDelete = { vaultToDelete = vault }
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

    if(showExportDialog){
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = {passphrase ->
                showExportDialog = false
                exportPassphrase = passphrase
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                exportLauncher.launch("lsqrd-backup-$date.lsqrd")
            }
        )
    }

    pendingImportUri?.let { uri ->
        if (showImportDialog){
            val fileName = uri.lastPathSegment?:"backup file"
            ImportDialog(
                fileName = fileName,
                onDismiss = {
                    showImportDialog = false
                    pendingImportUri = null
                },
                onImport = { passphrase ->
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
                        onError = {error ->
                            pendingImportUri = null
                            Toast.makeText(context, "Import failed: $error", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }

    vaultToDelete?.let { vault ->
        ConfirmDeleteDialog(
            title = "Delete vault",
            message = "Delete \"${vault.name}\"? This will also delete all its credentials and fields.",
            onConfirm = {
                viewModel.deleteVault(vault)
                vaultToDelete = null
            },
            onDismiss = { vaultToDelete = null }
        )
    }

    vaultToEdit?.let { vault ->
        EditVaultDialog(
            currentName = vault.name,
            onDismiss = { vaultToEdit = null },
            onConfirm = { newName ->
                viewModel.updateVault(vault.copy(name = newName))
                vaultToEdit = null
            }
        )
    }
}

@Composable
fun VaultRow(vault: Vault, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(vault.name, fontWeight = FontWeight.Medium) },
        leadingContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Vault")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Vault")
                }
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider()
}