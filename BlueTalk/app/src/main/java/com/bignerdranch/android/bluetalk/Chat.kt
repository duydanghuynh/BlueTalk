package com.bignerdranch.android.bluetalk

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Chat(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val fromUserId: Int,
    val toUserId: Int,
    var messages: MutableList<Message>
)
