package com.yveskalume.alertapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yveskalume.alertapp.database.dao.ContactDao
import com.yveskalume.alertapp.database.entities.Contact
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(private val contactDao: ContactDao) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = contactDao.getAllContactsStream().map {
        HomeUiState.Success(it)
    }.catch<HomeUiState> {
        HomeUiState.Error(it.message ?: "An error occurred")
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState.Loading
    )


    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            try {
                contactDao.delete(contact)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}