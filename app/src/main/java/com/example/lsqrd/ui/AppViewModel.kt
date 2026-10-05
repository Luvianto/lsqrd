package com.example.lsqrd.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lsqrd.data.AppDatabase
import com.example.lsqrd.data.BackupCredential
import com.example.lsqrd.data.BackupField
import com.example.lsqrd.data.BackupPayload
import com.example.lsqrd.data.BackupVault
import com.example.lsqrd.data.Credential

import com.example.lsqrd.data.CredentialField
import com.example.lsqrd.data.CredentialWithFields
import com.example.lsqrd.data.CryptoManager
import com.example.lsqrd.data.ExportManager
import com.example.lsqrd.data.Vault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ImportResult(val vaultsImported: Int, val credentialsImported: Int)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val vaultDao = db.vaultDao()
    private val credentialDao = db.credentialDao()
    private val credentialFieldDao = db.credentialFieldDao()

    // ── Vaults ──────────────────────────────────────────

    val vaults: StateFlow<List<Vault>> = vaultDao.getAllVaults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getVaultById(vaultId: Long): Flow<Vault?> =
        vaultDao.getVaultById(vaultId)

    fun addVault(name: String) = viewModelScope.launch {
        vaultDao.insert(Vault(name = name))
    }

    fun deleteVault(vault: Vault) = viewModelScope.launch {
        vaultDao.delete(vault)
    }

    fun updateVault(vault: Vault) = viewModelScope.launch {
        vaultDao.update(vault)
    }

    // ── Credentials ──────────────────────────────────────

    fun getCredentials(vaultId: Long): Flow<List<Credential>> =
        credentialDao.getAllCredentials(vaultId)

    fun addCredential(vaultId: Long, name: String) = viewModelScope.launch {
        credentialDao.insert(Credential(vaultId = vaultId, name = name))
    }

    fun deleteCredential(credential: Credential) = viewModelScope.launch {
        credentialDao.delete(credential)
    }

    fun updateCredential(credential: Credential) = viewModelScope.launch {
        credentialDao.update(credential)
    }

    // ── Fields ────────────────────────────────────────────

    fun getCredentialWithFields(credentialId: Long): Flow<CredentialWithFields> =
        credentialDao.getCredentialWithFields(credentialId).map { credentialWithFields ->
            credentialWithFields.copy(fields = credentialWithFields.fields.map { field ->
                field.copy(value = CryptoManager.decrypt(field.value))
            })
        }

    fun addField(credentialId: Long, label: String, value: String, isSecret: Boolean) =
        viewModelScope.launch {
            credentialFieldDao.insert(
                CredentialField(
                    credentialId = credentialId,
                    label = label,
                    value = CryptoManager.encrypt(value),
                    isSecret = isSecret
                )
            )
        }

    fun deleteField(field: CredentialField) = viewModelScope.launch {
        credentialFieldDao.delete(field)
    }

    fun updateField(field: CredentialField) = viewModelScope.launch {
        credentialFieldDao.update(field.copy(value = CryptoManager.encrypt(field.value)))
    }

    // ── Export / Import ───────────────────────────────────

    private fun uniqueName(name: String, existingNames: Set<String>): String {
        if (name !in existingNames) return name
        val imported = "$name (Imported)"
        if (imported !in existingNames) return imported
        var n = 2
        while ("$name (Imported $n)" in existingNames) n++
        return "$name (Imported $n)"
    }

    private suspend fun buildBackupPayload(): BackupPayload {
        val allVaults = vaultDao.getAllVaults().first()
        val backupVaults = allVaults.map { vault ->
            val credentials = credentialDao.getAllCredentials(vault.id).first()
            val backupCredentials = credentials.map { cred ->
                val withFields = credentialDao.getCredentialWithFields(cred.id).first()
                val backupFields = withFields.fields.map { field ->
                    BackupField(
                        label = field.label,
                        value = CryptoManager.decrypt(field.value),
                        isSecret = field.isSecret
                    )
                }
                BackupCredential(name = cred.name, fields = backupFields)
            }
            BackupVault(name = vault.name, credentials = backupCredentials)
        }
        return BackupPayload(vaults = backupVaults)
    }

    private suspend fun mergeBackup(payload: BackupPayload): ImportResult {
        val existingVaultNames = vaultDao.getAllVaults().first().map {
            it.name
        }.toSet()
        var vaultsCount = 0
        var credentialsCount = 0

        for (backupVault in payload.vaults) {
            val vaultName = uniqueName(backupVault.name, existingVaultNames.toMutableSet().also { })
            vaultDao.insert(Vault(name = vaultName))
            val newVaultId = vaultDao.getAllVaults().first().first { it.name == vaultName }.id
            vaultsCount++

            for (backupCred in backupVault.credentials) {
                credentialDao.insert(Credential(vaultId = newVaultId, name = backupCred.name))
                val newCredId = credentialDao.getAllCredentials(newVaultId).first().first {
                    it.name == backupCred.name
                }.id
                credentialsCount++

                for (backupField in backupCred.fields) {
                    credentialFieldDao.insert(
                        CredentialField(
                            credentialId = newCredId,
                            label = backupField.label,
                            value = CryptoManager.encrypt(backupField.value),
                            isSecret = backupField.isSecret
                        )
                    )
                }
            }
        }
        return ImportResult(vaultsCount, credentialsCount)
    }

    fun exportAll(
        passphrase: String,
        uri: Uri,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val payload = buildBackupPayload()
            ExportManager.encrypt(payload, passphrase, uri, context)
            withContext(Dispatchers.Main) { onSuccess() }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onError(e.message ?: "Export Failed") }
        }
    }

    fun importAll(
        passphrase: String,
        uri: Uri,
        context: Context,
        onSuccess: (ImportResult) -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val payload = ExportManager.decrypt(passphrase, uri, context)
            val result = mergeBackup(payload)
            withContext(Dispatchers.Main) { onSuccess(result) }
        } catch (e: SecurityException) {
            withContext(Dispatchers.Main) { onError("Wrong passphrase or invalid file") }
        }catch (e: Exception) {
            withContext(Dispatchers.Main) { onError(e.message ?: "Import Failed") }
        }
    }
}