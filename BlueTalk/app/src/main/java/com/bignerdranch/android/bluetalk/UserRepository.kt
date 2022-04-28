package com.bignerdranch.android.bluetalk

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.bluetalk.database.UserDatabase
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "bluetalk-database"
class UserRepository private constructor(context: Context) {
    private val database : UserDatabase = Room.databaseBuilder(
        context.applicationContext,
        UserDatabase::class.java,
        DATABASE_NAME
    ).build()
    private val userDao = database.userDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getUsers(): LiveData<List<User>> = userDao.getUsers()
    fun getUser(id: UUID): LiveData<User?> = userDao.getUser(id)

    fun updateUser(user: User) {
        executor.execute {
            userDao.updateUser(user)
        }
    }
    fun addUser(user: User) {
        executor.execute {
            userDao.addUser(user)
        }
    }

    companion object {
        private var INSTANCE: UserRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = UserRepository(context)
            }
        }
        fun get(): UserRepository {
            return INSTANCE ?:
            throw IllegalStateException("UserRepository must be initialized")
        }
    }
}