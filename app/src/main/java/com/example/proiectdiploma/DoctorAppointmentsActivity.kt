package com.example.proiectdiploma

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DoctorAppointmentsActivity : AppCompatActivity() {

    private lateinit var pendingAppointmentsLayout: LinearLayout
    private lateinit var acceptedAppointmentsLayout: LinearLayout
    private lateinit var userDetailsLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var doctorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_appointments)

        pendingAppointmentsLayout = findViewById(R.id.pendingAppointmentsLayout)
        acceptedAppointmentsLayout = findViewById(R.id.acceptedAppointmentsLayout)
        userDetailsLayout = findViewById(R.id.userDetailsLayout)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            doctorId = user.uid
            Log.d("DoctorAppointmentsActivity", "Doctor ID: $doctorId")
            loadPendingAppointments()
            loadAcceptedAppointments()
        } else {
            Log.e("DoctorAppointmentsActivity", "Doctor not logged in.")
            finish()
        }
    }

    private fun loadPendingAppointments() {
        doctorId?.let {
            Log.d("DoctorAppointmentsActivity", "Loading pending appointments for doctorId: $it")
            database.child("doctors").child(it).child("userPending")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        pendingAppointmentsLayout.removeAllViews() // Clear the layout first
                        if (snapshot.exists()) {
                            Log.d("DoctorAppointmentsActivity", "Pending appointments found: ${snapshot.childrenCount}")
                            for (appointmentSnapshot in snapshot.children) {
                                val userId = appointmentSnapshot.child("userId").value.toString()
                                val userName = appointmentSnapshot.child("userName").value?.toString() ?: "Unknown"
                                val date = appointmentSnapshot.child("date").value?.toString() ?: "Unknown"
                                val time = appointmentSnapshot.child("time").value?.toString() ?: "Unknown"

                                Log.d("DoctorAppointmentsActivity", "Pending appointment loaded: userId=$userId, userName=$userName, date=$date, time=$time")
                                addPendingAppointmentView(userId, userName, date, time, appointmentSnapshot.key!!)
                            }
                        } else {
                            Log.d("DoctorAppointmentsActivity", "No pending appointments found.")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DoctorAppointmentsActivity", "Failed to load pending appointments: ${error.message}")
                    }
                })
        }
    }

    private fun loadAcceptedAppointments() {
        doctorId?.let {
            Log.d("DoctorAppointmentsActivity", "Loading accepted appointments for doctorId: $it")
            database.child("doctors").child(it).child("acceptedAppointments")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        acceptedAppointmentsLayout.removeAllViews() // Clear the layout first
                        if (snapshot.exists()) {
                            Log.d("DoctorAppointmentsActivity", "Accepted appointments found: ${snapshot.childrenCount}")
                            for (appointmentSnapshot in snapshot.children) {
                                val userId = appointmentSnapshot.child("userId").value.toString()
                                val userName = appointmentSnapshot.child("userName").value?.toString() ?: "Unknown"
                                val date = appointmentSnapshot.child("date").value?.toString() ?: "Unknown"
                                val time = appointmentSnapshot.child("time").value?.toString() ?: "Unknown"

                                Log.d("DoctorAppointmentsActivity", "Accepted appointment loaded: userId=$userId, userName=$userName, date=$date, time=$time")
                                addAcceptedAppointmentView(userId, userName, date, time, appointmentSnapshot.key!!)
                            }
                        } else {
                            Log.d("DoctorAppointmentsActivity", "No accepted appointments found.")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DoctorAppointmentsActivity", "Failed to load accepted appointments: ${error.message}")
                    }
                })
        }
    }

    private fun addPendingAppointmentView(userId: String, userName: String, date: String, time: String, appointmentKey: String) {
        Log.d("DoctorAppointmentsActivity", "Adding pending appointment view for userId: $userId, userName: $userName, date: $date, time: $time")

        val appointmentView = View.inflate(this, R.layout.item_pending_appointment, null)

        val userNameTextView = appointmentView.findViewById<TextView>(R.id.userNameTextView)
        val dateTextView = appointmentView.findViewById<TextView>(R.id.dateTextView)
        val timeTextView = appointmentView.findViewById<TextView>(R.id.timeTextView)
        val acceptButton = appointmentView.findViewById<Button>(R.id.acceptButton)
        val refuseButton = appointmentView.findViewById<Button>(R.id.refuseButton)

        userNameTextView.text = userName
        dateTextView.text = date
        timeTextView.text = time

        acceptButton.setOnClickListener {
            Log.d("DoctorAppointmentsActivity", "Accepting user: $userName ($userId)")
            acceptUser(userId, userName, date, time, appointmentKey)
        }

        refuseButton.setOnClickListener {
            Log.d("DoctorAppointmentsActivity", "Refusing user: $userName ($userId)")
            refuseUser(userId, appointmentKey)
        }

        pendingAppointmentsLayout.addView(appointmentView)
    }

    private fun addAcceptedAppointmentView(userId: String, userName: String, date: String, time: String, appointmentKey: String) {
        Log.d("DoctorAppointmentsActivity", "Adding accepted appointment view for userId: $userId, userName: $userName, date: $date, time: $time")

        val appointmentView = View.inflate(this, R.layout.item_accepted_appointment, null)

        val userNameTextView = appointmentView.findViewById<TextView>(R.id.userNameTextView)
        val dateTextView = appointmentView.findViewById<TextView>(R.id.dateTextView)
        val timeTextView = appointmentView.findViewById<TextView>(R.id.timeTextView)
        val detailsButton = appointmentView.findViewById<Button>(R.id.detailsButton)

        userNameTextView.text = userName
        dateTextView.text = date
        timeTextView.text = time

        detailsButton.setOnClickListener {
            Log.d("DoctorAppointmentsActivity", "Loading details for userId: $userId")
            loadUserDetails(userId)
        }

        acceptedAppointmentsLayout.addView(appointmentView)
    }

    private fun acceptUser(userId: String, userName: String, date: String, time: String, appointmentKey: String) {
        val appointmentData = mapOf(
            "userId" to userId,
            "userName" to userName,
            "date" to date,
            "time" to time
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
                                    Log.d("DoctorAppointmentsActivity", "User accepted successfully: $userName ($userId)")
                                    loadPendingAppointments()
                                    loadAcceptedAppointments()
                                } else {
                                    Log.e("DoctorAppointmentsActivity", "Failed to create notification: ${task3.exception?.message}")
                                }
                            }
                        } else {
                            Log.e("DoctorAppointmentsActivity", "Failed to remove pending user: ${task2.exception?.message}")
                        }
                    }
                } else {
                    Log.e("DoctorAppointmentsActivity", "Failed to accept user: ${task.exception?.message}")
                }
            }
        }
    }

    private fun refuseUser(userId: String, appointmentKey: String) {
        doctorId?.let {
            database.child("doctors").child(it).child("userPending").child(appointmentKey).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("DoctorAppointmentsActivity", "User refused successfully: $userId")
                        loadPendingAppointments()
                    } else {
                        Log.e("DoctorAppointmentsActivity", "Failed to refuse user: ${task.exception?.message}")
                    }
                }
        }
    }

    private fun loadUserDetails(userId: String) {
        userDetailsLayout.removeAllViews()
        Log.d("DoctorAppointmentsActivity", "Loading user details for userId: $userId")
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("DoctorAppointmentsActivity", "User details found: ${snapshot.value}")

                    val userDetailsView = View.inflate(this@DoctorAppointmentsActivity, R.layout.user_item, null)

                    val textViewEmail = userDetailsView.findViewById<TextView>(R.id.textViewEmail)
                    val textViewLatitude = userDetailsView.findViewById<TextView>(R.id.textViewLatitude)
                    val textViewLongitude = userDetailsView.findViewById<TextView>(R.id.textViewLongitude)
                    val textViewUserType = userDetailsView.findViewById<TextView>(R.id.textViewUserType)
                    val textViewName = userDetailsView.findViewById<TextView>(R.id.textViewName)
                    val textViewCity = userDetailsView.findViewById<TextView>(R.id.textViewCity)
                    val textViewAllergies = userDetailsView.findViewById<TextView>(R.id.textViewAllergies)
                    val textViewSymptoms = userDetailsView.findViewById<TextView>(R.id.textViewSymptoms)
                    val textViewProblems = userDetailsView.findViewById<TextView>(R.id.textViewProblems)
                    val textViewDiagnosis = userDetailsView.findViewById<TextView>(R.id.textViewDiagnosis)
                    val textViewUnmatchedSymptoms = userDetailsView.findViewById<TextView>(R.id.textViewUnmatchedSymptoms)
                    val textViewAge = userDetailsView.findViewById<TextView>(R.id.textViewAge)
                    val textViewUsername = userDetailsView.findViewById<TextView>(R.id.textViewUsername)
                    val editTextDiseaseResult = userDetailsView.findViewById<EditText>(R.id.editTextDiseaseResult)
                    val buttonSaveDiseaseResult = userDetailsView.findViewById<Button>(R.id.buttonSaveDiseaseResult)

                    textViewEmail.text = snapshot.child("email").value?.toString() ?: "Unknown"
                    textViewLatitude.text = snapshot.child("latitude").value?.toString() ?: "Unknown"
                    textViewLongitude.text = snapshot.child("longitude").value?.toString() ?: "Unknown"
                    textViewUserType.text = snapshot.child("userType").value?.toString() ?: "Unknown"
                    textViewName.text = snapshot.child("name").value?.toString() ?: "Unknown"
                    textViewCity.text = snapshot.child("city").value?.toString() ?: "Unknown"
                    textViewAllergies.text = snapshot.child("allergies").value?.toString() ?: "Unknown"
                    textViewSymptoms.text = snapshot.child("symptoms").children.joinToString(", ") { it.value.toString() }
                    textViewProblems.text = snapshot.child("problems").value?.toString() ?: "Unknown"
                    textViewDiagnosis.text = snapshot.child("diseaseResult").value?.toString() ?: "Unknown"
                    textViewUnmatchedSymptoms.text = snapshot.child("unmatchedSymptoms").children.joinToString(", ") { it.value.toString() }
                    textViewAge.text = snapshot.child("age").value?.toString() ?: "Unknown"
                    textViewUsername.text = snapshot.child("username").value?.toString() ?: "Unknown"

                    editTextDiseaseResult.setText(snapshot.child("diseaseResult").value?.toString() ?: "")

                    buttonSaveDiseaseResult.setOnClickListener {
                        val newDiseaseResult = editTextDiseaseResult.text.toString()
                        Log.d("DoctorAppointmentsActivity", "Saving new disease result for userId: $userId, newDiseaseResult: $newDiseaseResult")
                        saveDiseaseResult(userId, newDiseaseResult)
                    }

                    userDetailsLayout.addView(userDetailsView)
                } else {
                    Log.e("DoctorAppointmentsActivity", "Failed to load user details: Snapshot does not exist")
                    Toast.makeText(this@DoctorAppointmentsActivity, "Failed to load user details.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DoctorAppointmentsActivity", "Failed to load user details: ${error.message}")
            }
        })
    }

    private fun saveDiseaseResult(userId: String, newDiseaseResult: String) {
        database.child("users").child(userId).child("diseaseResult").setValue(newDiseaseResult).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("DoctorAppointmentsActivity", "Disease result updated successfully for userId: $userId")
                Toast.makeText(this, "Disease result updated successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("DoctorAppointmentsActivity", "Failed to update disease result: ${task.exception?.message}")
                Toast.makeText(this, "Failed to update disease result.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
