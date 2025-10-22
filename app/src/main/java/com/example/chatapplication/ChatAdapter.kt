package com.example.chatapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    val chats: ArrayList<Message>,
    private val meUid: String = FirebaseRefs.requireUid()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val OUTGOING = 0
        private const val INCOMING = 1
    }

    // Create a single formatter instance (cheaper than new each bind)
    private val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun getItemViewType(position: Int): Int {
        val msg = chats[position]
        return if (msg.senderID == meUid) OUTGOING else INCOMING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == OUTGOING) {
            OutgoingVH(inflater.inflate(R.layout.outgoing_chat_row, parent, false))
        } else {
            IncomingVH(inflater.inflate(R.layout.incoming_chat_row, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = chats[position]
        val time = formatTime(msg.timestamp)

        when (holder) {
            is OutgoingVH -> {
                holder.myMSG.text = msg.msgContent
                holder.myTIME.text = time
            }
            is IncomingVH -> {
                holder.yourMSG.text = msg.msgContent
                holder.yourTIME.text = time
            }
        }
    }

    override fun getItemCount(): Int = chats.size

    private fun formatTime(ts: Long): String =
        if (ts > 0L) timeFmt.format(ts) else "—"

    class OutgoingVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val myMSG: TextView = itemView.findViewById(R.id.outgoingmsg)
        val myTIME: TextView = itemView.findViewById(R.id.outgoingtime)
    }

    class IncomingVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val yourMSG: TextView = itemView.findViewById(R.id.incomingmsg)
        val yourTIME: TextView = itemView.findViewById(R.id.incomingtime)
    }
}
