package com.example.lsqrd.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.lsqrd.data.CredentialField
import com.example.lsqrd.data.CredentialWithFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDetailScreen(
    viewModel: AppViewModel,
    credentialId: Long,
    onBack: () -> Unit
) {
    val credentialWithFields by produceState<CredentialWithFields?>(
        initialValue = null,
        key1 = credentialId
    ) {
        viewModel.getCredentialWithFields(credentialId).collect { value = it }
    }

    val fields = credentialWithFields?.fields ?: emptyList()

    var visibleFieldsId by remember(credentialId) { mutableStateOf(emptySet<Long>()) }

    var showAddDialog by remember { mutableStateOf(false) }

    var fieldToDelete by remember { mutableStateOf<CredentialField?>(null) }
    var fieldToEdit by remember { mutableStateOf<CredentialField?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(credentialWithFields?.credential?.name ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add field")
            }
        }
    ) { innerPadding ->
        if (fields.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No fields yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(fields, key = { it.id }) { field ->
                    val isRevealed = field.id in visibleFieldsId
                    FieldRow(
                        field = field,
                        isRevealed = isRevealed,
                        onToggleVisibility = {
                            visibleFieldsId = if (isRevealed) {
                                visibleFieldsId - field.id
                            } else {
                                visibleFieldsId + field.id
                            }
                        },
                        onEdit = { fieldToEdit = field },
                        onDelete = { fieldToDelete = field }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddFieldDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { label, value, isSecret ->
                viewModel.addField(credentialId, label, value, isSecret)
                showAddDialog = false
            }
        )
    }

    fieldToDelete?.let { field ->
        ConfirmDeleteDialog(
            title = "Delete field",
            message = "Delete \"${field.label}\"?",
            onConfirm = {
                viewModel.deleteField(field)
                fieldToDelete = null
            },
            onDismiss = { fieldToDelete = null }
        )
    }

    fieldToEdit?.let { field ->
        EditFieldDialog(
            currentLabel = field.label,
            currentValue = field.value,
            currentIsSecret = field.isSecret,
            onDismiss = { fieldToEdit = null },
            onConfirm = { label, value, isSecret ->
                viewModel.updateField(field.copy(label = label, value = value, isSecret = isSecret))
                fieldToEdit = null
            }
        )
    }
}

@Composable
fun FieldRow(
    field: CredentialField,
    isRevealed: Boolean,
    onToggleVisibility: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val displayValue = if (field.isSecret && !isRevealed) "••••••••" else field.value

    ListItem(
        headlineContent = { Text(field.label, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(displayValue, modifier = Modifier.clickable {
                clipboardManager.setText(AnnotatedString(field.value))
                Toast.makeText(context, "${field.label} copied", Toast.LENGTH_SHORT).show()
            })
        },
        trailingContent = {
            Row {
                if (field.isSecret) {
                    TextButton(onClick = onToggleVisibility) {
                        Text(if (isRevealed) "Hide" else "Show")
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Field")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Field")
                }
            }
        }
    )
    HorizontalDivider()
}