package com.bignerdranch.android.bluetalk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class UserDetailViewModel: ViewModel() {
    private val userRepository = UserRepository.get()
    private val userIdLiveData = MutableLiveData<UUID>()
    var userLiveData: LiveData<User?> =
        Transformations.switchMap(userIdLiveData) { crimeId ->
            userRepository.getUser(crimeId)
        }
    fun loadUser(crimeId: UUID) {
        userIdLiveData.value = crimeId
    }

    fun saveUser(user: User) {
        userRepository.updateUser(user)
    }

}