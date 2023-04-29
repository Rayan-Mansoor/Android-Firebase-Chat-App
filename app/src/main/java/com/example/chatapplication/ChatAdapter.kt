package com.example.chatapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat

class ChatAdapter(val chats : ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //    class ChatViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
//        val message = itemView.findViewById<TextView>(R.id.msg)
//        val time = itemView.findViewById<TextView>(R.id.time)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
//        return ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.incoming_chat_row,parent,false))
//    }
//
//    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
//        holder.message.text = chats[position].msgContent
//        val formattedDate = SimpleDateFormat("hh:mm a").format(chats[position].timestamp)
//        holder.time.text = formattedDate
//    }
//
//    override fun getItemCount(): Int {
//        return chats.size
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0){
            return outgoingChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.outgoing_chat_row,parent,false))
        }
        if (viewType == 1){
            return incomingChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.incoming_chat_row,parent,false))
        }
        return outgoingChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.outgoing_chat_row,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is outgoingChatViewHolder){
            holder.myMSG.text = chats[position].msgContent
            val formattedDate = SimpleDateFormat("hh:mm a").format(chats[position].timestamp)
            holder.myTIME.text = formattedDate
        }
        if (holder is incomingChatViewHolder){
            holder.yourMSG.text = chats[position].msgContent
            val formattedDate = SimpleDateFormat("hh:mm a").format(chats[position].timestamp)
            holder.yourTIME.text = formattedDate

        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chats[position].senderID == FirebaseAuth.getInstance().currentUser!!.uid){
            0
        } else
            1
    }

    class outgoingChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val myMSG = itemView.findViewById<TextView>(R.id.outgoingmsg)
        val myTIME = itemView.findViewById<TextView>(R.id.outgoingtime)
    }

    class incomingChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val yourMSG = itemView.findViewById<TextView>(R.id.incomingmsg)
        val yourTIME = itemView.findViewById<TextView>(R.id.incomingtime)
    }
}