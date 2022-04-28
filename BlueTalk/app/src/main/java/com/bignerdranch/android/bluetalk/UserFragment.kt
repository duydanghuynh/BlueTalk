package com.bignerdranch.android.bluetalk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.Observer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
//private const val TAG = "UserFragment"
private const val ARG_USER_ID = "user_id"
class UserFragment: Fragment() {
    private lateinit var user: User
    private lateinit var userNameEditText: EditText
    private lateinit var deviceNameEditText: EditText
    private lateinit var deviceAddressEditText: EditText
    private lateinit var deviceSaveButton: Button
    private val userDetailViewModel: UserDetailViewModel by lazy {
        ViewModelProviders.of(this).get(UserDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = User()
        val userId: UUID = arguments?.getSerializable(ARG_USER_ID) as UUID
//        Log.d(TAG, "args bundle crime ID: $crimeId")
//    // Eventually, load user from database
        userDetailViewModel.loadUser(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        passing in the layout resource ID. The second parameter is your view’s parent, which is usually needed to configure the widgets properly.
//        The third parameter tells the layout inflater whether to immediately add the inflated view to the view’s parent.
//        You pass in false because the fragment’s view will be hosted in the activity’s container view
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        userNameEditText = view.findViewById(R.id.username_editText)
        deviceNameEditText = view.findViewById(R.id.device_name_editText)
        deviceAddressEditText = view.findViewById(R.id.device_address_editText)
        deviceSaveButton = view.findViewById(R.id.device_save_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userDetailViewModel.userLiveData.observe(
            viewLifecycleOwner,
            Observer { user ->
                user?.let {
                    this.user = user
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }
            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                user.name = sequence.toString()
            }
            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }
        userNameEditText.addTextChangedListener(titleWatcher)
        deviceSaveButton.setOnClickListener {
            // callDAO
//            user.deviceName = deviceNameEditText.text.toString()
//            user.deviceAddress = deviceAddressEditText.text.toString()
        }
    }
    override fun onStop() {
        super.onStop()
        userDetailViewModel.saveUser(user)
    }


    private fun updateUI() {
        userNameEditText.setText(user.name)
        deviceNameEditText.setText(user.deviceName)
        deviceAddressEditText.setText(user.deviceAddress)
        deviceSaveButton.apply {
            jumpDrawablesToCurrentState()
        }
    }

    companion object {
        fun newInstance(crimeId: UUID): UserFragment {
            val args = Bundle().apply {
                putSerializable(ARG_USER_ID, crimeId)
            }
            return UserFragment().apply {
                arguments = args
            }
        }
    }

}