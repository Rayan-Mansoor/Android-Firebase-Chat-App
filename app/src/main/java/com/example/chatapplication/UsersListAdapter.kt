package com.example.chatapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsersListAdapter(val usersList : ArrayList<User>) : RecyclerView.Adapter<UsersListAdapter.UserListViewHolder>() {

    class UserListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val userName = itemView.findViewById<TextView>(R.id.user_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        return UserListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_row,parent,false))
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        holder.userName.text = usersList[position].userEmail
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context,ChatActivity::class.java)
            intent.putExtra("ID",usersList[position].userID)
            intent.putExtra("Name",usersList[position].userEmail)
            it.context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

}