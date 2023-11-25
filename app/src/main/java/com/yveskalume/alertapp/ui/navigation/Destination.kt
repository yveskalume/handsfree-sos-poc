package com.yveskalume.alertapp.ui.navigation

import androidx.navigation.NavController

enum class Destination(val route: String) {
    PhoneNumber("phone-number"),
    Otp("otp"),
    Auth("auth"),
    Home("home"),
    AddContact("add-contact"),
    EditContact("edit-contact")
}

fun NavController.navigate(destination: Destination) {
    navigate(destination.route)
}