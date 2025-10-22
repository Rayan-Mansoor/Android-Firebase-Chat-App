package com.example.chatapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsersListAdapter(private val usersList: ArrayList<User>) :
    RecyclerView.Adapter<UsersListAdapter.UserListViewHolder>() {

    class UserListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val userName: TextView = itemView.findViewById(R.id.user_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        return UserListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.user_row,parent,false)
        )
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val row = usersList[position]
        holder.userName.text = row.userEmail
        holder.itemView.setOnClickListener {
            val me = FirebaseRefs.requireUid()
            val channelId = getChannelId(me, row.userID)
            val ctx = it.context
            val intent = Intent(ctx, ChatActivity::class.java).apply {
                putExtra("ID", row.userID)
                putExtra("Name", row.userEmail)
                putExtra("channelId", channelId)
            }
            ctx.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = usersList.size
}