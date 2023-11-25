package com.yveskalume.alertapp.di

import android.content.Context
import com.yveskalume.alertapp.database.AppDataBase

class AppContainer(context: Context) {

    private val roomDatabase = AppDataBase.getInstance(context)

    val contactDao = roomDatabase.contactDao()


}