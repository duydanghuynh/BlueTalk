package com.bignerdranch.android.bluetalk
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class User(
    @PrimaryKey val id:  UUID = UUID.randomUUID(),
    var name: String="",
//    var isActivated: Boolean = true,//1:activated, 0:deactivated
    var deviceName: String="",
    var deviceAddress: String="",
    var isOnline: Boolean = false,
)
