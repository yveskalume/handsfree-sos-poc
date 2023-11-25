package com.yveskalume.alertapp.ui.screen.addcontact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yveskalume.alertapp.ui.theme.AlertAppTheme

@Composable
fun AddContactRoute(onContactAdded: () -> Unit, viewModel: AddContactViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is AddContactUiState.Success) {
            onContactAdded()
        }
    }
    AddContact(
        uiState = uiState,
        onSubmit = { name, lastName, phoneNumber ->
            viewModel.addContact(name, lastName, phoneNumber)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContact(
    uiState: AddContactUiState,
    onSubmit: (name: String, lastName: String, phoneNumber: String) -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var lastName by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember {
        mutableStateOf(TextFieldValue("+243", selection = TextRange(5, 5)))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "New Contact") }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Name")
                }
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Last Name")
                }
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    if (it.text.length in 4..13) {
                        phoneNumber = it
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword
                ),
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Phone Number")
                }
            )

            Button(
                enabled = name.isNotBlank()
                        && lastName.isNotBlank()
                        && phoneNumber.text.length == 13
                        && (uiState is AddContactUiState.Idle || uiState is AddContactUiState.Error),
                onClick = { onSubmit(name.trim(), lastName.trim(), phoneNumber.text.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(text = "Save")
                Spacer(modifier = Modifier.width(4.dp))
                AnimatedVisibility(
                    visible = uiState is AddContactUiState.Loading,
                    modifier = Modifier.size(20.dp)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview
@Composable
fun AddContactPreview() {
    AlertAppTheme {
        AddContact(onSubmit = { _, _, _ -> }, uiState = AddContactUiState.Idle)
    }
}