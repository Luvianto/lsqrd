package com.example.lsqrd.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lsqrd.data.Entry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    viewModel: EntryViewModel = viewModel()
) {
    val entries by viewModel.entries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Password Manager") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add entry")
            }
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No entries yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(entries, key = { it.id }) { entry ->
                    EntryRow(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog (
            onDismiss = { showAddDialog = false },
            onConfirm = { site, username, password ->
                viewModel.addEntry(site, username, password)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EntryRow(entry: Entry, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(entry.siteName) },
        supportingContent = { Text(entry.username) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    )
    HorizontalDivider()
}