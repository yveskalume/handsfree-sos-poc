package com.yveskalume.alertapp.ui.screen.home

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yveskalume.alertapp.R
import com.yveskalume.alertapp.database.entities.Contact
import com.yveskalume.alertapp.service.TrackingService
import com.yveskalume.alertapp.ui.component.ContactItem
import com.yveskalume.alertapp.ui.theme.AlertAppTheme
import com.yveskalume.alertapp.util.PrefKey
import com.yveskalume.alertapp.util.dataStore
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    onAddContact: () -> Unit,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onAddContact = onAddContact,
        onDeleteContact = { contact ->
            viewModel.deleteContact(contact)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onAddContact: () -> Unit,
    onDeleteContact: (Contact) -> Unit,
) {

    var launched by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val homePreferenceHolder = rememberHomePreferenceHolder(context = context)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val isAlerting by homePreferenceHolder.alerting.collectAsState(initial = false)
    val isTrackingEnabled by homePreferenceHolder.trackingEnabled.collectAsState(initial = false)

    val cardColor by animateColorAsState(
        targetValue = if (isAlerting == true) Color(0xFFA42F2F) else Color(0xFF2D8E29),
        label = ""
    )

    val cardTile by remember {
        derivedStateOf {
            if (isAlerting == true) R.string.title_alerting else R.string.title_tracking
        }
    }

    val cardDescription by remember {
        derivedStateOf {
            if (isAlerting == true) {
                R.string.txt_alerts_are_being_sent
            } else {
                R.string.txt_your_phone_is_being_tracked
            }
        }
    }

    val serviceItent = remember {
        Intent(context, TrackingService::class.java)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alertify",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "New contact") },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                onClick = onAddContact
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Card(
                modifier = Modifier
                    .zIndex(2f)
                    .align(Alignment.BottomStart)
                    .size(180.dp)
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        if (isTrackingEnabled == true && isAlerting == false) {
                            coroutineScope.launch {
                                launched = true
//                                context.stopService(serviceItent)
                                context.dataStore.edit {
                                    it[PrefKey.TRACKING] = true
                                    it[PrefKey.ALERTING] = true
                                }
                                context.startForegroundService(serviceItent)
                            }
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                content = {}
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isTrackingEnabled == true) {
                    item {
                        Card(
                            onClick = {
                                if (isAlerting == true) {
                                    context.stopService(serviceItent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement(),
                            colors = CardDefaults.cardColors(
                                containerColor = cardColor,
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = stringResource(id = cardTile),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = cardDescription),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Enable tracking",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Switch(
                                checked = isTrackingEnabled == true,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        context.startForegroundService(serviceItent)
                                    } else {
                                        context.stopService(serviceItent)
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    Text(text = "Contacts", modifier = Modifier.fillMaxWidth())
                }

                if (uiState is HomeUiState.Success) {
                    items(uiState.contacts, key = { it.phone }) { contact ->
                        ContactItem(
                            contact = contact,
                            onDelete = { onDeleteContact(contact) },
                        )
                    }
                } else if (uiState is HomeUiState.Loading) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    AlertAppTheme {
        HomeScreen(
            onAddContact = {},
            uiState = HomeUiState.Success(listOf()),
            onDeleteContact = {},
        )
    }
}