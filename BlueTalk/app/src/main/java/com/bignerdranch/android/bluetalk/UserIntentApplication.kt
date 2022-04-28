package com.bignerdranch.android.bluetalk

import android.app.Application

class UserIntentApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        UserRepository.initialize(this)
    }
}