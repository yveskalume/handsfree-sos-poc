package com.yveskalume.alertapp

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yveskalume.alertapp.ui.navigation.Destination
import com.yveskalume.alertapp.ui.navigation.navigate
import com.yveskalume.alertapp.ui.screen.addcontact.AddContactRoute
import com.yveskalume.alertapp.ui.screen.addcontact.AddContactViewModel
import com.yveskalume.alertapp.ui.screen.home.HomeRoute
import com.yveskalume.alertapp.ui.screen.home.HomeViewModel
import com.yveskalume.alertapp.ui.theme.AlertAppTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        askPermissions()
        setContent {
            AlertAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Destination.Home.route
                    ) {

                        composable(Destination.Home.route) {
                            HomeRoute(
                                onAddContact = { navController.navigate(Destination.AddContact) },
                                viewModel = viewModel(
                                    initializer = {
                                        HomeViewModel(
                                            contactDao = (application as App).appContainer.contactDao
                                        )
                                    }
                                )
                            )
                        }
                        composable(Destination.AddContact.route) {
                            AddContactRoute(
                                onContactAdded = { navController.navigateUp() },
                                viewModel = viewModel(
                                    initializer = {
                                        AddContactViewModel(
                                            contactDao = (application as App).appContainer.contactDao
                                        )
                                    }
                                )
                            )
                        }
                    }

                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askPermissions() {
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.containsValue(false)) {
                Toast.makeText(
                    this,
                    "Certaines permissions n'ont pas ete accordee",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        launcher.launch(
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).toTypedArray()
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }
}