package com.yveskalume.alertapp.ui.screen.addcontact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yveskalume.alertapp.database.dao.ContactDao
import com.yveskalume.alertapp.database.entities.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class AddContactViewModel(private val contactDao: ContactDao) : ViewModel() {

    private val _uiState: MutableStateFlow<AddContactUiState> =
        MutableStateFlow(AddContactUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun addContact(name: String, lastName: String, phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = AddContactUiState.Loading
            val contact = Contact(
                name = name,
                lastName = lastName,
                phone = phoneNumber,
                createdAt = Date()
            )

            try {
                contactDao.insert(contact)
                _uiState.value = AddContactUiState.Success
            } catch (e: Exception) {
                _uiState.value = AddContactUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}