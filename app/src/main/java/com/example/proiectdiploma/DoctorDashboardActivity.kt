package com.example.proiectdiploma

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DoctorDashboardActivity : AppCompatActivity() {

    private lateinit var doctorRequestsLayout: LinearLayout
    private lateinit var acceptedUsersLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var doctorId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        doctorRequestsLayout = findViewById(R.id.doctorRequestsLayout)
        acceptedUsersLayout = findViewById(R.id.acceptedUsersLayout)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            doctorId = user.uid
            Log.d("DoctorDashboardActivity", "Doctor ID: $doctorId")
            loadDoctorRequests()
            loadAcceptedUsers()
        } else {
            Log.e("DoctorDashboardActivity", "No user logged in.")
        }
    }

    private fun loadDoctorRequests() {
        Log.d("DoctorDashboardActivity", "Loading doctor requests for doctor ID: $doctorId")
        database.child("doctors").child(doctorId).child("userPending")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    doctorRequestsLayout.removeAllViews()
                    if (snapshot.exists()) {
                        for (appointmentSnapshot in snapshot.children) {
                            val userId = appointmentSnapshot.child("userId").value.toString()
                            val userName = appointmentSnapshot.child("userName").value.toString()
                            Log.d("DoctorDashboardActivity", "Request found: $userName ($userId)")
                            addRequestToLayout(userId, userName)
                        }
                    } else {
                        Log.d("DoctorDashboardActivity", "No requests found for doctor ID: $doctorId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DoctorDashboardActivity", "Failed to load doctor requests: ${error.message}")
                }
            })
    }

    private fun loadAcceptedUsers() {
        Log.d("DoctorDashboardActivity", "Loading accepted users for doctor ID: $doctorId")
        database.child("doctors").child(doctorId).child("acceptedAppointments")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    acceptedUsersLayout.removeAllViews()
                    if (snapshot.exists()) {
                        for (appointmentSnapshot in snapshot.children) {
                            val userId = appointmentSnapshot.child("userId").value.toString()
                            Log.d("DoctorDashboardActivity", "Accepted user found: $userId")
                            loadUserDetails(userId)
                        }
                    } else {
                        Log.d("DoctorDashboardActivity", "No accepted users found for doctor ID: $doctorId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DoctorDashboardActivity", "Failed to load accepted users: ${error.message}")
                }
            })
    }

    private fun loadUserDetails(userId: String) {
        Log.d("DoctorDashboardActivity", "Loading details for user ID: $userId")
        database.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.child("name").value.toString()
                        val userEmail = snapshot.child("email").value.toString()
                        val userCity = snapshot.child("city").value.toString()
                        val userSymptoms = snapshot.child("symptoms").children.joinToString(", ") { it.value.toString() }
                        val userDiseaseResult = snapshot.child("diseaseResult").value.toString()
                        Log.d("DoctorDashboardActivity", "User details loaded: $userName ($userId)")
                        addAcceptedUserToLayout(userId, userName, userEmail, userCity, userSymptoms, userDiseaseResult)
                    } else {
                        Log.d("DoctorDashboardActivity", "No details found for user ID: $userId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DoctorDashboardActivity", "Failed to load user details: ${error.message}")
                }
            })
    }

    private fun addRequestToLayout(userId: String, userName: String) {
        val userLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        val userTextView = TextView(this).apply {
            text = userName
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val acceptButton = Button(this).apply {
            text = "Accept"
            setOnClickListener {
                Log.d("DoctorDashboardActivity", "Accepting user: $userName ($userId)")
                acceptUser(userId, userName)
            }
        }

        val refuseButton = Button(this).apply {
            text = "Refuse"
            setOnClickListener {
                Log.d("DoctorDashboardActivity", "Refusing user: $userName ($userId)")
                refuseUser(userId)
            }
        }

        userLayout.addView(userTextView)
        userLayout.addView(acceptButton)
        userLayout.addView(refuseButton)

        doctorRequestsLayout.addView(userLayout)
    }

    private fun addAcceptedUserToLayout(userId: String, userName: String, userEmail: String, userCity: String, userSymptoms: String, userDiseaseResult: String) {
        val userLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val userNameTextView = TextView(this).apply {
            text = "Name: $userName"
            textSize = 16f
        }

        val userEmailTextView = TextView(this).apply {
            text = "Email: $userEmail"
            textSize = 16f
        }

        val userCityTextView = TextView(this).apply {
            text = "City: $userCity"
            textSize = 16f
        }

        val userSymptomsTextView = TextView(this).apply {
            text = "Symptoms: $userSymptoms"
            textSize = 16f
        }

        val userDiseaseResultEditText = EditText(this).apply {
            setText(userDiseaseResult)
            textSize = 16f
        }

        val saveButton = Button(this).apply {
            text = "Save"
            setOnClickListener {
                Log.d("DoctorDashboardActivity", "Saving disease result for user: $userId")
                saveDiseaseResult(userId, userDiseaseResultEditText.text.toString())
            }
        }

        userLayout.addView(userNameTextView)
        userLayout.addView(userEmailTextView)
        userLayout.addView(userCityTextView)
        userLayout.addView(userSymptomsTextView)
        userLayout.addView(userDiseaseResultEditText)
        userLayout.addView(saveButton)

        acceptedUsersLayout.addView(userLayout)
    }

    private fun saveDiseaseResult(userId: String, diseaseResult: String) {
        database.child("users").child(userId).child("diseaseResult").setValue(diseaseResult)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DoctorDashboardActivity", "Disease result updated successfully for user $userId.")
                } else {
                    Log.e("DoctorDashboardActivity", "Failed to update disease result for user $userId: ${task.exception?.message}")
                }
            }
    }

    private fun acceptUser(userId: String, userName: String) {
        database.child("doctors").child(doctorId).child("acceptedAppointments").child(userId).setValue(userName)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DoctorDashboardActivity", "User accepted successfully: $userName ($userId)")
                    database.child("doctors").child(doctorId).child("userPending").child(userId).removeValue()
                    loadDoctorRequests()
                    loadAcceptedUsers()
                } else {
                    Log.e("DoctorDashboardActivity", "Failed to accept user: ${task.exception?.message}")
                }
            }
    }

    private fun refuseUser(userId: String) {
        database.child("doctors").child(doctorId).child("userPending").child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DoctorDashboardActivity", "User refused successfully: $userId")
                    loadDoctorRequests()
                } else {
                    Log.e("DoctorDashboardActivity", "Failed to refuse user: ${task.exception?.message}")
                }
            }
    }
}
