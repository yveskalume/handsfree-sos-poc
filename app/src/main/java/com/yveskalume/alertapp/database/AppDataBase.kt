package com.yveskalume.alertapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yveskalume.alertapp.database.converters.Converters
import com.yveskalume.alertapp.database.dao.ContactDao
import com.yveskalume.alertapp.database.entities.Contact

@Database(entities = [Contact::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var instance: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDataBase::class.java, "alertapp"
                ).build().also {
                    instance = it
                }
            }
        }


    }
}