package com.example.chatapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.databinding.ActivityChatBinding
import com.example.chatapplication.databinding.ActivityMainBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var databaseRefUsers : DatabaseReference
    private lateinit var databaseRefCurrentChannel : DatabaseReference
    private lateinit var databaseRefCurrentChannelReverse : DatabaseReference

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatsList : ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityChatBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val recieverID = intent.getStringExtra("ID")
        val recieverName = intent.getStringExtra("Name")

        databaseRefUsers = FirebaseDatabase.getInstance("https://chat-application-803f3-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")
        databaseRefCurrentChannel = FirebaseDatabase.getInstance("https://chat-application-803f3-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Channels").child(FirebaseAuth.getInstance().currentUser!!.uid).child(recieverID!!)
        databaseRefCurrentChannelReverse = FirebaseDatabase.getInstance("https://chat-application-803f3-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Channels").child(recieverID!!).child(FirebaseAuth.getInstance().currentUser!!.uid)

        binding.floatingActionButton.setOnClickListener {
            val typedMSG = binding.editTextTextPersonName.text.toString()
            val timestamp = Date()
            val msg = Message(FirebaseAuth.getInstance().currentUser!!.uid,typedMSG,timestamp.time)

            databaseRefCurrentChannel.push().setValue(msg)
            databaseRefCurrentChannelReverse.push().setValue(msg)
            binding.editTextTextPersonName.text = null
        }

        chatsList = ArrayList()
        chatAdapter = ChatAdapter(chatsList)
        binding.chatrcv.adapter = chatAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatrcv.layoutManager = layoutManager


        databaseRefCurrentChannel.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsList.clear()
                for (snap in snapshot.children){
                    val msgRetrieved = snap.getValue(Message::class.java)
                    if (msgRetrieved != null) {
                        chatsList.add(msgRetrieved)
                    }
                }
                chatAdapter.notifyDataSetChanged()
                if(chatAdapter.chats.size != 0){
                    binding.chatrcv.smoothScrollToPosition(chatsList.size-1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}