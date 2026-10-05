package com.example.lsqrd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (passphrase: String) -> Unit
) {
    var passphrase by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    val mismatch = passphrase.isNotEmpty() && confirm.isNotEmpty() && passphrase != confirm

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Vaults") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Export passphrase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPass) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = { showPass = !showPass }
                        ) { Text(if (showPass) "Hide" else "Show") }
                    }
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm passphrase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPass) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    isError = mismatch,
                    supportingText = if (mismatch) ({ Text("Passphrases do not match") }) else null
                )
                Text(
                    "Keep this passphrase safe - you'll need it to import your data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onExport(passphrase) },
                enabled = passphrase.isNotBlank() && passphrase == confirm
            ) { Text("Export") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel")}
        }
    )
}

@Composable
fun ImportDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onImport: (passphrase: String) -> Unit
) {
    var passphrase by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Vaults") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "File: $fileName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Export passphrase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPass) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = { showPass = !showPass }
                        ) { Text(if (showPass) "Hide" else "Show") }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onImport(passphrase) },
                enabled = passphrase.isNotBlank()
            ) { Text("Import") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel")}
        }
    )
}