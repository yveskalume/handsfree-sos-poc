package com.yveskalume.alertapp.ui.screen.addcontact

sealed interface AddContactUiState {
    object Idle : AddContactUiState
    object Loading : AddContactUiState
    object Success : AddContactUiState
    data class Error(val message: String) : AddContactUiState
}