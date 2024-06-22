package com.example.proiectdiploma

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DoctorPendingAppointmentsActivity : AppCompatActivity() {

    private lateinit var pendingAppointmentsLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var doctorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_pending_appointments)

        pendingAppointmentsLayout = findViewById(R.id.pendingAppointmentsLayout)
        val buttonGoToHomePage: Button = findViewById(R.id.buttonGoToHomePage)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            doctorId = user.uid
            Log.d("DoctorPendingAppointmentsActivity", "Doctor ID: $doctorId")
            loadPendingAppointments()
        } else {
            Log.e("DoctorPendingAppointmentsActivity", "Doctor not logged in.")
            finish()
        }
        buttonGoToHomePage.setOnClickListener {
            val intent = Intent(this, DoctorHomePage::class.java)
            startActivity(intent)
        }
    }

    private fun loadPendingAppointments() {
        doctorId?.let {
            Log.d("DoctorPendingAppointmentsActivity", "Loading pending appointments for doctorId: $it")
            database.child("doctors").child(it).child("userPending")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        pendingAppointmentsLayout.removeAllViews()
                        if (snapshot.exists()) {
                            Log.d("DoctorPendingAppointmentsActivity", "Pending appointments found: ${snapshot.childrenCount}")
                            for (appointmentSnapshot in snapshot.children) {
                                val userId = appointmentSnapshot.child("userId").value.toString()
                                val userName = appointmentSnapshot.child("userName").value?.toString() ?: "Unknown"
                                val date = appointmentSnapshot.child("date").value?.toString() ?: "Unknown"
                                val time = appointmentSnapshot.child("time").value?.toString() ?: "Unknown"
                                val disease = appointmentSnapshot.child("userDiseaseResult").value?.toString() ?: "Unknown"

                                Log.d("DoctorPendingAppointmentsActivity", "Pending appointment loaded: userId=$userId, userName=$userName, date=$date, time=$time, disease=$disease")
                                addPendingAppointmentView(userId, userName, date, time, disease, appointmentSnapshot.key!!)
                            }
                        } else {
                            Log.d("DoctorPendingAppointmentsActivity", "No pending appointments found.")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DoctorPendingAppointmentsActivity", "Failed to load pending appointments: ${error.message}")
                    }
                })
        }
    }

    private fun addPendingAppointmentView(userId: String, userName: String, date: String, time: String, disease: String, appointmentKey: String) {
        Log.d("DoctorPendingAppointmentsActivity", "Adding pending appointment view for userId: $userId, userName: $userName, date: $date, time: $time, disease: $disease")

        val appointmentView = View.inflate(this, R.layout.item_pending_appointment, null)

        val userNameTextView = appointmentView.findViewById<TextView>(R.id.userNameTextView)
        val dateTextView = appointmentView.findViewById<TextView>(R.id.dateTextView)
        val timeTextView = appointmentView.findViewById<TextView>(R.id.timeTextView)
        val diseaseTextView = appointmentView.findViewById<TextView>(R.id.diseaseTextView)
        val acceptButton = appointmentView.findViewById<Button>(R.id.acceptButton)
        val refuseButton = appointmentView.findViewById<Button>(R.id.refuseButton)

        userNameTextView.text = userName
        dateTextView.text = date
        timeTextView.text = time
        diseaseTextView.text = disease

        acceptButton.setOnClickListener {
            Log.d("DoctorPendingAppointmentsActivity", "Accepting user: $userName ($userId)")
            acceptUser(userId, userName, date, time, disease, appointmentKey)
        }

        refuseButton.setOnClickListener {
            Log.d("DoctorPendingAppointmentsActivity", "Refusing user: $userName ($userId)")
            refuseUser(userId, appointmentKey)
        }

        pendingAppointmentsLayout.addView(appointmentView)
    }

    private fun acceptUser(userId: String, userName: String, date: String, time: String, disease: String, appointmentKey: String) {
        val appointmentData = mapOf(
            "userId" to userId,
            "userName" to userName,
            "date" to date,
            "time" to time,
            "userDiseaseResult" to disease
        )
        doctorId?.let {
            val acceptedAppointmentRef = database.child("doctors").child(it).child("acceptedAppointments").child(appointmentKey)
            val userPendingRef = database.child("doctors").child(it).child("userPending").child(appointmentKey)
            val notificationRef = database.child("users").child(userId).child("notifications").push()

            val notificationData = mapOf(
                "status" to "accepted",
                "message" to "Your appointment has been accepted.",
                "doctorId" to it,
                "appointmentId" to appointmentKey,
                "timestamp" to ServerValue.TIMESTAMP
            )

            acceptedAppointmentRef.setValue(appointmentData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userPendingRef.removeValue().addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            notificationRef.setValue(notificationData).addOnCompleteListener { task3 ->
                                if (task3.isSuccessful) {
                                    Log.d("DoctorPendingAppointmentsActivity", "User accepted successfully: $userName ($userId)")
                                    loadPendingAppointments()
                                } else {
                                    Log.e("DoctorPendingAppointmentsActivity", "Failed to create notification: ${task3.exception?.message}")
                                }
                            }
                        } else {
                            Log.e("DoctorPendingAppointmentsActivity", "Failed to remove pending user: ${task2.exception?.message}")
                        }
                    }
                } else {
                    Log.e("DoctorPendingAppointmentsActivity", "Failed to accept user: ${task.exception?.message}")
                }
            }
        }
    }

    private fun refuseUser(userId: String, appointmentKey: String) {
        doctorId?.let {
            database.child("doctors").child(it).child("userPending").child(appointmentKey).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("DoctorPendingAppointmentsActivity", "User refused successfully: $userId")
                        loadPendingAppointments()
                    } else {
                        Log.e("DoctorPendingAppointmentsActivity", "Failed to refuse user: ${task.exception?.message}")
                    }
                }
        }
    }
}
