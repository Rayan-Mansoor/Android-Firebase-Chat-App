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
            var emailToSearch = ""
            val emailET = EditText(this)
            emailET.inputType = InputType.TYPE_CLASS_TEXT
            emailET.hint = "Enter the email of that person"
            val builder = AlertDialog.Builder(this).setView(emailET)
            builder.setPositiveButton("ADD") { _, _ ->
                emailToSearch = emailET.text.toString()
                Log.d("Main",emailToSearch)
                FirebaseRefs.users.whereEqualTo("userEmail", emailToSearch)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            Log.d("Main", "task succeeded")

                            // Get the first matching user
                            val userDoc = querySnapshot.documents[0]
                            val userId = userDoc.getString("userID") ?: ""
                            val userEmail = userDoc.getString("userEmail") ?: ""

                            Toast.makeText(this, "User found and has been added to your chat list", Toast.LENGTH_SHORT).show()

                            // Update channels collection
                            databaseRefChannels.child(FirebaseRefs.uid!!).child(userId).setValue("hello")
                            databaseRefChannels.child(userId).child(FirebaseRefs.uid!!).setValue("hello")

                            // Add user to list
                            val newUser = User(userId, userEmail)
                            addedUsers.add(newUser)
                            usersListAdapter.notifyDataSetChanged()

                        } else {
                            Toast.makeText(this, "No user found with the specified email", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Main", "task didn't succeed: ${exception.message}")
                        Toast.makeText(this, "Error searching for user", Toast.LENGTH_SHORT).show()
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

            databaseRefChannels = FirebaseDatabase.getInstance().getReference("channels")

            addedUsers  = ArrayList()

            val childRef = databaseRefChannels.child(FirebaseAuth.getInstance().currentUser!!.uid)
            childRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children){
                        FirebaseRefs.users.document(snap.key!!)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val userId = documentSnapshot.getString("userID") ?: ""
                                    val userEmail = documentSnapshot.getString("userEmail") ?: ""

                                    val retrievedUser = User(userId, userEmail)

                                    if (!addedUsers.contains(retrievedUser)) {
                                        addedUsers.add(retrievedUser)
                                        usersListAdapter.notifyDataSetChanged()
                                    }
                                } else {
                                    Log.d("Main", "User document not found")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.d("Main", "task didn't succeed: ${exception.message}")
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