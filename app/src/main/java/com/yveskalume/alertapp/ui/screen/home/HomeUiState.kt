package com.yveskalume.alertapp.ui.screen.home

import com.yveskalume.alertapp.database.entities.Contact

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val contacts: List<Contact>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}