package com.example.chatapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginStart
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseRefUsers : DatabaseReference
    private lateinit var databaseRefChannels : DatabaseReference


    private lateinit var usersListAdapter : UsersListAdapter
    private lateinit var addedUsers : ArrayList<User>

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.my_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout){
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
            return true
        }

        if (item.itemId == R.id.addPerson){
            databaseRefUsers = FirebaseDatabase.getInstance("https://chat-application-803f3-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")

            var emailToSearch = ""
            val emailET = EditText(this)
            emailET.inputType = InputType.TYPE_CLASS_TEXT
            emailET.hint = "Enter the email of that person"
            val builder = AlertDialog.Builder(this).setView(emailET)
            builder.setPositiveButton("ADD") { _, _ ->
                emailToSearch = emailET.text.toString()
                Log.d("Main",emailToSearch)
                databaseRefUsers.get().addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d("Main","task succeeded")
                        task.result.children.forEach {
                            if (emailToSearch == it.child("userEmail").value){
                                Toast.makeText(this,"User found and has been add to your chat list",Toast.LENGTH_SHORT).show()
                                databaseRefChannels.child(FirebaseAuth.getInstance().currentUser!!.uid).child(it.child("userID").value.toString()).setValue("hello")
                                databaseRefChannels.child(it.child("userID").value.toString()).child(FirebaseAuth.getInstance().currentUser!!.uid).setValue("hello")
                                val newUser = User(it.child("userID").value.toString(),it.child("userEmail").value.toString(),it.child("userPassword").value.toString())
                                addedUsers.add(newUser)
                                usersListAdapter.notifyDataSetChanged()
                            }

                            else
                                Toast.makeText(this,"No user found with the specified email",Toast.LENGTH_SHORT).show()
                        }
                    }
                    if(!task.isSuccessful){
                        Log.d("Main","task didn't succeed")
                    }


                }
            }.show()

        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()

        }
        else{
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            databaseRefUsers = FirebaseDatabase.getInstance("https://chat-application-803f3-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")

            databaseRefChannels = FirebaseDatabase.getInstance("https://chat-application-803f3-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Channels")

            addedUsers  = ArrayList()

            val childRef = databaseRefChannels.child(FirebaseAuth.getInstance().currentUser!!.uid)
            childRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children){
                        databaseRefUsers.get().addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                task.result.children.forEach {
                                    if (snap.key == it.child("userID").value){
                                        val retrievedUser = User(it.child("userID").value.toString(),it.child("userEmail").value.toString(),it.child("userPassword").value.toString())
                                        if (!addedUsers.contains(retrievedUser)){
                                            addedUsers.add(retrievedUser)
                                            usersListAdapter.notifyDataSetChanged()
                                        }

                                    }


                                }
                            }
                            if(!task.isSuccessful){
                                Log.d("Main","task didn't succeed")
                            }


                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Main","task cancelled")
                }
            })


            usersListAdapter = UsersListAdapter(addedUsers)
            binding.usersRcv.adapter = usersListAdapter
            binding.usersRcv.layoutManager = LinearLayoutManager(this)
        }






    }
}