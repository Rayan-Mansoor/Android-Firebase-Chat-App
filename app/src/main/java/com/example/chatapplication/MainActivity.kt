package com.example.chatapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapplication.databinding.ActivityMainBinding
import com.example.chatapplication.databinding.DialogAddUserBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var usersListAdapter: UsersListAdapter

    // We’ll render "conversations" as a list of the other user (email) like before
    private val rows = ArrayList<User>()
    private var fsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val me = FirebaseRefs.uid
        if (me == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish(); return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usersListAdapter = UsersListAdapter(rows)
        binding.usersRcv.apply {
            adapter = usersListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.fabAdd.setOnClickListener {
            showAddPersonDialog()
        }

        // ✅ Listen to Firestore channels where I'm a member
        fsListener = FirebaseRefs.channels
            .whereArrayContains("members", me)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("Main", "Firestore listen error: ${err.message}"); return@addSnapshotListener
                }
                rows.clear()
                if (snap != null) {
                    for (doc in snap.documents) {
                        val members = (doc.get("members") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val otherUid = members.firstOrNull { it != me } ?: me
                        FirebaseRefs.users.document(otherUid).get()
                            .addOnSuccessListener { userDoc ->
                                val email = userDoc.getString("userEmail") ?: "(no email)"
                                val u = User(userID = otherUid, userEmail = email)
                                if (!rows.any { it.userID == u.userID }) {
                                    rows.add(u)
                                    usersListAdapter.notifyDataSetChanged()
                                }
                            }
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        fsListener?.remove()
    }

    // ===== Menu: logout + add person =====

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.my_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddPersonDialog() {
        val me = FirebaseRefs.requireUid()

        // Inflate custom dialog layout using ViewBinding
        val dialogBinding = DialogAddUserBinding.inflate(layoutInflater)

        // Create Material dialog
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Add button click
        dialogBinding.addButton.setOnClickListener {
            val email = dialogBinding.emailEditText.text.toString().trim()

            // Validate email
            if (email.isEmpty()) {
                dialogBinding.emailInputLayout.error = "Email is required"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialogBinding.emailInputLayout.error = "Invalid email format"
                return@setOnClickListener
            }

            // Clear any previous errors
            dialogBinding.emailInputLayout.error = null

            // Show progress, disable buttons
            dialogBinding.progressBar.visibility = View.VISIBLE
            dialogBinding.addButton.isEnabled = false
            dialogBinding.cancelButton.isEnabled = false
            dialogBinding.emailEditText.isEnabled = false

            FirebaseRefs.users.whereEqualTo("userEmail", email).limit(1).get()
                .addOnSuccessListener { q ->
                    if (q.isEmpty) {
                        // Hide progress, re-enable UI
                        dialogBinding.progressBar.visibility = View.GONE
                        dialogBinding.addButton.isEnabled = true
                        dialogBinding.cancelButton.isEnabled = true
                        dialogBinding.emailEditText.isEnabled = true

                        dialogBinding.emailInputLayout.error = "No user found with that email"
                        return@addOnSuccessListener
                    }

                    val otherUid = q.documents[0].getString("userID") ?: return@addOnSuccessListener
                    val channelId = getChannelId(me, otherUid)

                    // Create/merge Firestore channel document
                    val chDoc = FirebaseRefs.channels.document(channelId)
                    chDoc.get().addOnSuccessListener { snap ->
                        val base = mutableMapOf<String, Any>(
                            "members" to listOf(me, otherUid),
                            "kind" to "dm",
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                        if (!snap.exists()) {
                            base["createdAt"] = FieldValue.serverTimestamp()
                        }
                        chDoc.set(base, SetOptions.merge())
                            .addOnSuccessListener {
                                // Ensure my membership in RTDB
                                FirebaseRefs.memberships.child(channelId).child(me).setValue(true)

                                // Dismiss dialog
                                dialog.dismiss()

                                // Navigate to chat
                                val i = Intent(this, ChatActivity::class.java).apply {
                                    putExtra("ID", otherUid)
                                    putExtra("Name", email)
                                    putExtra("channelId", channelId)
                                }
                                startActivity(i)
                            }
                            .addOnFailureListener {
                                dialogBinding.progressBar.visibility = View.GONE
                                dialogBinding.addButton.isEnabled = true
                                dialogBinding.cancelButton.isEnabled = true
                                dialogBinding.emailEditText.isEnabled = true

                                Toast.makeText(this, "Error creating chat", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    // Hide progress, re-enable UI
                    dialogBinding.progressBar.visibility = View.GONE
                    dialogBinding.addButton.isEnabled = true
                    dialogBinding.cancelButton.isEnabled = true
                    dialogBinding.emailEditText.isEnabled = true

                    Toast.makeText(this, "Error searching for user", Toast.LENGTH_SHORT).show()
                }
        }

        // Cancel button click
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}