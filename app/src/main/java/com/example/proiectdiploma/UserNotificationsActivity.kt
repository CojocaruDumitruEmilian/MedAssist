package com.example.proiectdiploma

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserNotificationsActivity : AppCompatActivity() {

    private lateinit var notificationsLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_notifications)

        notificationsLayout = findViewById(R.id.notificationsLayout)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            userId = user.uid
            loadNotifications()
        } else {
            Log.e("UserNotificationsActivity", "User not logged in.")
            finish()
        }
    }

    private fun loadNotifications() {
        userId?.let {
            database.child("users").child(it).child("notifications").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (notificationSnapshot in snapshot.children) {
                        val notification = notificationSnapshot.child("message").value as? String ?: "Unknown notification"
                        val notificationView = TextView(this@UserNotificationsActivity).apply {
                            text = notification
                            textSize = 16f
                        }
                        notificationsLayout.addView(notificationView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserNotificationsActivity", "Failed to load notifications: ${error.message}")
                }
            })
        }
    }
}
