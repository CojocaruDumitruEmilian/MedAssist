package com.example.proiectdiploma

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DoctorHomePage : AppCompatActivity() {

    private lateinit var buttonAcceptedUsers: Button
    private lateinit var buttonPendingUsers: Button
    private lateinit var buttonLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_home_page)

        buttonAcceptedUsers = findViewById(R.id.buttonAcceptedUsers)
        buttonPendingUsers = findViewById(R.id.buttonPendingUsers)
        buttonLogout = findViewById(R.id.logout)

        buttonAcceptedUsers.setOnClickListener {
            val intent = Intent(this, DoctorAcceptedAppointmentsActivity::class.java)
            startActivity(intent)
        }

        buttonPendingUsers.setOnClickListener {
            val intent = Intent(this, DoctorPendingAppointmentsActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Optional: call finish() to remove this activity from the back stack
        }
    }
}
