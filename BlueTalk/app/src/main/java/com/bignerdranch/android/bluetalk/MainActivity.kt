package com.bignerdranch.android.bluetalk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import java.util.*

private const val TAG="MainActivity >>"
class MainActivity : AppCompatActivity(),UserListFragment.Callbacks {
    private lateinit var textViewFriends :TextView
    //(in memory,not visible, and not active in the foreground)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called - in memory,not visible, and not active in the foreground")
        setContentView(R.layout.activity_main)
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
//            val fragment = UserFragment()
            val fragment = UserListFragment.newInstance()//update MainActivity to host an instance of CrimeListFragment instead of CrimeFragment.
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onUserSelected(userId: UUID) {
//        Log.d(TAG, "MainActivity.onUserSelected: $userId")
//        val fragment = UserFragment()
        val fragment = UserFragment.newInstance(userId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) //When you add a transaction to the back stack, this means that when the user presses the Back button the transaction will be reversed.
        // So, in this case, UerFragment will be replaced with UserListFragment.
            .commit()
    }

    //(in memory, visible, and not active in the foreground)
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called - in memory, visible, and not active in the foreground")
    }

    //(in memory, visible, and active in the foreground)
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called - in memory, visible, and active in the foreground")
    }

    //(in memory,visible, and not active in the foreground)
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called - in memory,visible partially, and not active in the foreground")
    }

    //(in memory, not visible, and not active in the foreground)
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called - in memory, not visible, and not active in the foreground")
    }

    //(not in memory, not visible, and not active in the foreground)
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called - not in memory, not visible, and not active in the foreground")
    }
}