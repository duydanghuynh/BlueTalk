package com.bignerdranch.android.bluetalk

import androidx.room.Entity
import java.time.LocalDateTime
@Entity
data class Message(
//    val id:Int,
    val sequenceNumber:Int,
    val fromUserId: Int,
    val toUserId: Int,
    val content: String,
    val dateTime: LocalDateTime
//    var isSent: Boolean = false
)