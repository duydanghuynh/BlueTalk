package com.bignerdranch.android.bluetalk.database

import androidx.room.Dao
import androidx.room.Query
import com.bignerdranch.android.bluetalk.Message
import java.util.*

@Dao
interface MessageDao {
    @Query("SELECT * FROM message")
    fun getUsers(): List<Message>

    @Query("SELECT * FROM message WHERE id=(:id)")
    fun getUser(id: UUID): Message?
}