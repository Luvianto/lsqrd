package com.example.lsqrd.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lsqrd.data.AppDatabase
import com.example.lsqrd.data.Credential
import com.example.lsqrd.data.CredentialField
import com.example.lsqrd.data.CredentialWithFields
import com.example.lsqrd.data.Vault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
        credentialDao.getCredentialWithFields(credentialId)

    fun addField(credentialId: Long, label: String, value: String, isSecret: Boolean) =
        viewModelScope.launch {
            credentialFieldDao.insert(
                CredentialField(
                    credentialId = credentialId,
                    label = label,
                    value = value,
                    isSecret = isSecret
                )
            )
        }

    fun deleteField(field: CredentialField) = viewModelScope.launch {
        credentialFieldDao.delete(field)
    }

    fun updateField(field: CredentialField) = viewModelScope.launch {
        credentialFieldDao.update(field)
    }
}