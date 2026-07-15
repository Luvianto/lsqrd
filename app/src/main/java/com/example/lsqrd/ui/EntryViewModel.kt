package com.example.lsqrd.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lsqrd.data.AppDatabase
import com.example.lsqrd.data.Entry
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).entryDao()

    val entries: StateFlow<List<Entry>> = dao.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addEntry(siteName: String, username: String, password: String, notes: String = "") {
        viewModelScope.launch {
            dao.insert(
                Entry(
                    siteName = siteName,
                    username = username,
                    encryptedPassword = password,
                    notes = notes
                )
            )
        }
    }

    fun updateEntry(entry: Entry) {
        viewModelScope.launch {
            dao.update(entry)
        }
    }

    fun deleteEntry(entry: Entry) {
        viewModelScope.launch {
            dao.delete(entry)
        }
    }
}