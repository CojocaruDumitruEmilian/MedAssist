package com.example.proiectdiploma

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminActivity : AppCompatActivity() {

    private lateinit var listViewUsers: ListView
    private lateinit var database: DatabaseReference
    private lateinit var usersList: MutableList<User>
    private lateinit var btnCommonDiseases: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        listViewUsers = findViewById(R.id.listViewUsers)
        database = FirebaseDatabase.getInstance().reference.child("users")
        usersList = mutableListOf()
        btnCommonDiseases=findViewById(R.id.btnCommonDiseases)

        btnCommonDiseases.setOnClickListener {
            Log.d("AdminActivity", "Button clicked")
            val intent = Intent(this, CommonDiseasesActivity::class.java)
            startActivity(intent)
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        usersList.add(user)
                    }
                }
                val adapter = UserAdapter(this@AdminActivity, usersList)
                listViewUsers.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminActivity", "Failed to read users", error.toException())
            }
        })
    }
}
