package com.bignerdranch.android.bluetalk

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "UserListFragment"
class UserListFragment:Fragment() {
/**
 * Required interface for hosting activities
 */
    interface Callbacks {
        fun onUserSelected(userId: UUID)
    }
    private var callbacks: Callbacks? = null

    private lateinit var userRecyclerView: RecyclerView
    private var adapter: UserAdapter? = UserAdapter(emptyList())
    private val userListViewModel: UserListViewModel by lazy {
        ViewModelProviders.of(this).get(UserListViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)
        userRecyclerView =view.findViewById(R.id.user_recycler_view) as RecyclerView
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        userRecyclerView.adapter=adapter
        return view
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userListViewModel.userListLiveData.observe(
            viewLifecycleOwner,
            Observer { users ->
                users?.let {
                    Log.i(TAG, "Got users ${users.size}")
                    updateUI(users)
                }
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    //    private fun updateUI() {
    private fun updateUI(users: List<User>) {
//        val users = userListViewModel.users
        adapter = UserAdapter(users)
        userRecyclerView.adapter = adapter
    }

    private inner class UserHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var user: User
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_title)
        private val deviceNameTextView: TextView = itemView.findViewById(R.id.device_name_title)
        init {
            itemView.setOnClickListener(this)
        }
        fun bind(user: User) {
            this.user = user
            usernameTextView.text = user.name
            deviceNameTextView.text = user.deviceAddress
        }

        override fun onClick(view: View?) {
//            Toast.makeText(context, "${user.name} pressed!", Toast.LENGTH_SHORT).show()
            callbacks?.onUserSelected(user.id)
        }
    }

    private inner class UserAdapter(var users: List<User>)
        : RecyclerView.Adapter<UserHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : UserHolder {
            val view = layoutInflater.inflate(R.layout.list_item_user, parent, false)
            return UserHolder(view)
        }
        override fun getItemCount() = users.size
        override fun onBindViewHolder(holder: UserHolder, position: Int) {
            val user = users[position]
            holder.bind(user)
        }
    }

    companion object {
        fun newInstance(): UserListFragment {
            return UserListFragment()
        }
    }

}