package com.bignerdranch.android.bluetalk.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bignerdranch.android.bluetalk.User
import java.util.*

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
//    fun getUsers(): List<User>
    fun getUsers(): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE id=(:id)")
//    fun getUser(id: UUID): User
    fun getUser(id: UUID): LiveData<User?>

    @Insert
    fun addUser(user:User)

    @Update
    fun updateUser(user:User)

//    @Delete
//    fun deleteUser(user:User)
}