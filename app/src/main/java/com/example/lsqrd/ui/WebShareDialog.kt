package com.example.lsqrd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lsqrd.data.CredentialField
import com.example.lsqrd.data.LocalWebServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun WebShareDialog(
    credentialName: String,
    fields: List<CredentialField>,
    onDismiss: () -> Unit
) {
    val token = remember {
        (1..8).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
    }
    var server by remember { mutableStateOf<LocalWebServer?>(null) }
    var port by remember { mutableStateOf(0) }

    var url by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableIntStateOf(300) }

    LaunchedEffect(Unit) {
        try {
            val p = withContext(Dispatchers.IO) { LocalWebServer.findAvailablePort() }
            port = p
            val s = LocalWebServer(p, credentialName, fields, token)
            withContext(Dispatchers.IO) { s.start() }
            server = s
            val ip = LocalWebServer.getLocalIpAddress()
            if (ip == null) {
                errorMsg = "Not connected to Wi-Fi. Connect and try again."
                return@LaunchedEffect
            }
            url = "http://$ip:$port/$token"
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            runCatching { s.stop() }
            onDismiss()
        } catch (e: Exception) {
            errorMsg = "Could not start the server: ${e.message}"
        }
    }

    DisposableEffect(Unit) {
        onDispose { runCatching { server?.stop() } }
    }

    AlertDialog(
        onDismissRequest = { runCatching { server?.stop() }; onDismiss() },
        title = { Text("Send to computer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    errorMsg != null -> Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                    url == null -> CircularProgressIndicator()
                    else -> {
                        Text(
                            "Open this URL in your computer's browser:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SelectionContainer {
                            Text(
                                text = url!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "Expires in %d:%02d".format(timeLeft / 60, timeLeft % 60),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Both devices must be on the same Wi-Fi network.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { runCatching { server?.stop() }; onDismiss() }) {
                Text("Stop")
            }
        },
    )
}