package com.bignerdranch.android.bluetalk

import androidx.lifecycle.ViewModel

class UserListViewModel:ViewModel() {
//    val users = mutableListOf<User>()
//    init {
//        for (i in 0 until 10) {
//            val user = User()
//            user.name = "User #$i"
//            user.deviceName = "Motorola #$i"
//            user.deviceAddress="$i$i:$i$i:$i$i:$i$i"
//            users+=user
//        }
//    }

    private val userRepository = UserRepository.get()
    val userListLiveData = userRepository.getUsers()
}
