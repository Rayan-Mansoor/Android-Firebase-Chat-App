package com.example.chatapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.databinding.ActivityChatBinding
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesRef: DatabaseReference

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatsList: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityChatBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val me = FirebaseRefs.requireUid()
        val receiverId = intent.getStringExtra("ID") ?: return
        val receiverName = intent.getStringExtra("Name") ?: ""
        val passedChannelId = intent.getStringExtra("channelId")
        val channelId = passedChannelId ?: getChannelId(me, receiverId)

        // 1) Ensure Firestore channel doc exists / is updated (idempotent)
        val chDoc = FirebaseRefs.channels.document(channelId)
        chDoc.get().addOnSuccessListener { snap ->
            if (snap.exists()) {
                // Update last-seen metadata only
                chDoc.set(
                    mapOf("updatedAt" to FieldValue.serverTimestamp()),
                    SetOptions.merge()
                )
            } else {
                // Create channel document (DM)
                chDoc.set(
                    mapOf(
                        "members" to listOf(me, receiverId),
                        "kind" to "dm",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
        }

        // 2) Ensure my RTDB membership exists (receiver will create theirs on first open)
        FirebaseRefs.memberships.child(channelId).child(me).setValue(true)

        // 3) Messages live at /messages/<channelId>
        messagesRef = FirebaseRefs.messages.child(channelId)

        binding.floatingActionButton.setOnClickListener {
            val text = binding.editTextTextPersonName.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            val msg = Message(
                senderID = me,
                msgContent = text,
                timestamp = Date().time
            )
            messagesRef.push().setValue(msg)
            binding.editTextTextPersonName.text = null

            // optional: bump Firestore updatedAt for sorting lists
            FirebaseRefs.channels.document(channelId)
                .set(mapOf("updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
        }

        chatsList = ArrayList()
        chatAdapter = ChatAdapter(chatsList)
        binding.chatrcv.adapter = chatAdapter
        binding.chatrcv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }

        // 4) Live stream messages
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsList.clear()
                for (snap in snapshot.children) {
                    snap.getValue(Message::class.java)?.let { chatsList.add(it) }
                }
                chatAdapter.notifyDataSetChanged()
                if (chatAdapter.chats.isNotEmpty()) {
                    binding.chatrcv.smoothScrollToPosition(chatsList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) { /* no-op */ }
        })
    }
}