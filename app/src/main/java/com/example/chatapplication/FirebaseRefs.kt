// FirebaseRefs.kt
package com.example.chatapplication

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

object FirebaseRefs {

    // Singletons (lazy + thread-safe)
    val auth: FirebaseAuth by lazy { Firebase.auth }
    val rtdb: FirebaseDatabase by lazy { Firebase.database}
    val fs: FirebaseFirestore by lazy { Firebase.firestore }

    // Common paths (RTDB)
    val channels get() = rtdb.reference.child("channels")
    val presence get() = rtdb.reference.child("presence")
    val typing get() = rtdb.reference.child("typing")

    // Firestore collections
    val users get() = fs.collection("users")

    // Handy getters (don’t cache—auth state can change)
    val uid: String? get() = auth.currentUser?.uid

    fun requireUid(): String =
        uid ?: error("Not signed in")
}

fun getChannelId(a: String, b: String): String =
    if (a <= b) "${a}_$b" else "${b}_${a}"
