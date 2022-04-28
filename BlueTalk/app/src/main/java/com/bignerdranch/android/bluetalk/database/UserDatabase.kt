package com.bignerdranch.android.bluetalk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.bluetalk.User

@Database(entities = [ User::class ], version=1,exportSchema = false)
@TypeConverters(UserTypeConverters::class)
abstract class UserDatabase:RoomDatabase(){
    abstract fun userDao(): UserDao
}
