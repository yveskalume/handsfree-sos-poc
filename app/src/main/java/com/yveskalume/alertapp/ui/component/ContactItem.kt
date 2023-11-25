package com.yveskalume.alertapp.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yveskalume.alertapp.database.entities.Contact
import com.yveskalume.alertapp.ui.theme.AlertAppTheme
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactItem(
    contact: Contact,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val swipeToDismissState = rememberDismissState(
        positionalThreshold = { it * 0.5f },
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart) {
                showDialog = true
            }
            true
        },
    )

    LaunchedEffect(contact) {
        swipeToDismissState.reset()
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                coroutineScope.launch {
                    swipeToDismissState.reset()
                }
                showDialog = false
            },
            text = { Text("Are you sure you want to delete this contact?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete();
                        showDialog = false
                    }
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            swipeToDismissState.reset()
                        }
                        showDialog = false
                    }
                ) {
                    Text("CANCEL")
                }
            },
        )
    }

    SwipeToDismiss(
        directions = setOf(DismissDirection.EndToStart),
        state = swipeToDismissState,
        background = {
            Card(
                modifier = modifier,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFAC3E36))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Delete",
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        )
                    }
                }
            }
        },
        dismissContent = {
            Card(modifier = modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = RoundedCornerShape(16.dp)) {
                        Text(
                            text = "${contact.name.first()}${contact.lastName.first()}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column(modifier = Modifier.wrapContentHeight()) {
                        Text(
                            text = "${contact.name} ${contact.lastName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = contact.phone,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun ContactItemPreview() {
    AlertAppTheme {
        ContactItem(
            contact = Contact(1, "Yves", "Kalume", "+243970000000", createdAt = Date()),
            onDelete = {},
        )
    }
}