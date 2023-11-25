package com.yveskalume.alertapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.yveskalume.alertapp.database.entities.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY createdAt DESC")
    suspend fun getAllContacts() : List<Contact>

    @Query("SELECT * FROM contacts ORDER BY createdAt DESC")
    fun getAllContactsStream(): Flow<List<Contact>>

    @Insert
    suspend fun insert(contact: Contact)

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Int): Contact

    @Delete
    suspend fun delete(contact: Contact)
}