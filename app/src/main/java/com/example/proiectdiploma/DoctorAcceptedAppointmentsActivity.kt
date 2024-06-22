package com.example.proiectdiploma

import android.content.Intent
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

class DoctorAcceptedAppointmentsActivity : AppCompatActivity() {

    private lateinit var acceptedAppointmentsLayout: LinearLayout
    private lateinit var userDetailsLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var doctorId: String? = null
    private var isDoctor: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_accepted_appointments)

        userDetailsLayout = findViewById(R.id.userDetailsLayout)
        acceptedAppointmentsLayout = findViewById(R.id.acceptedAppointmentsLayout)
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val buttonGoToHomePage: Button = findViewById(R.id.buttonGoToHomePage)
        buttonGoToHomePage.setOnClickListener {
            val intent = Intent(this, DoctorHomePage::class.java)
            startActivity(intent)
        }

        val user = auth.currentUser
        if (user != null) {
            doctorId = user.uid
            Log.d("DoctorAcceptedAppointmentsActivity", "Doctor ID: $doctorId")
            checkIfDoctor(doctorId!!)
        } else {
            Log.e("DoctorAcceptedAppointmentsActivity", "Doctor not logged in.")
            finish()
        }
    }

    private fun checkIfDoctor(userId: String) {
        database.child("doctors").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isDoctor = true
                    loadAcceptedAppointments()
                } else {
                    Toast.makeText(this@DoctorAcceptedAppointmentsActivity, "User is not a doctor", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DoctorAcceptedAppointmentsActivity", "Failed to check user type: ${error.message}")
                finish()
            }
        })
    }

    private fun loadAcceptedAppointments() {
        doctorId?.let {
            Log.d("DoctorAcceptedAppointmentsActivity", "Loading accepted appointments for doctorId: $it")
            database.child("doctors").child(it).child("acceptedAppointments")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        acceptedAppointmentsLayout.removeAllViews()
                        if (snapshot.exists()) {
                            Log.d("DoctorAcceptedAppointmentsActivity", "Accepted appointments found: ${snapshot.childrenCount}")
                            for (appointmentSnapshot in snapshot.children) {
                                val userId = appointmentSnapshot.child("userId").value.toString()
                                val userName = appointmentSnapshot.child("userName").value?.toString() ?: "Unknown"
                                val date = appointmentSnapshot.child("date").value?.toString() ?: "Unknown"
                                val time = appointmentSnapshot.child("time").value?.toString() ?: "Unknown"

                                Log.d("DoctorAcceptedAppointmentsActivity", "Accepted appointment loaded: userId=$userId, userName=$userName, date=$date, time=$time")
                                addAcceptedAppointmentView(userId, userName, date, time, appointmentSnapshot.key!!)
                            }
                        } else {
                            Log.d("DoctorAcceptedAppointmentsActivity", "No accepted appointments found.")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DoctorAcceptedAppointmentsActivity", "Failed to load accepted appointments: ${error.message}")
                    }
                })
        }
    }

    private fun addAcceptedAppointmentView(userId: String, userName: String, date: String, time: String, appointmentKey: String) {
        Log.d("DoctorAcceptedAppointmentsActivity", "Adding accepted appointment view for userId: $userId, userName: $userName, date: $date, time: $time")

        val appointmentView = View.inflate(this, R.layout.item_accepted_appointment, null)

        val userNameTextView = appointmentView.findViewById<TextView>(R.id.userNameTextView)
        val dateTextView = appointmentView.findViewById<TextView>(R.id.dateTextView)
        val timeTextView = appointmentView.findViewById<TextView>(R.id.timeTextView)
        val detailsButton = appointmentView.findViewById<Button>(R.id.detailsButton)

        userNameTextView.text = userName
        dateTextView.text = date
        timeTextView.text = time

        detailsButton.setOnClickListener {
            Log.d("DoctorAcceptedAppointmentsActivity", "Loading details for userId: $userId")
            loadUserDetails(userId)
        }

        acceptedAppointmentsLayout.addView(appointmentView)
    }

    private fun loadUserDetails(userId: String) {
        userDetailsLayout.removeAllViews()
        Log.d("DoctorAcceptedAppointmentsActivity", "Loading user details for userId: $userId")
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("DoctorAcceptedAppointmentsActivity", "User details found: ${snapshot.value}")

                    val userDetailsView = View.inflate(this@DoctorAcceptedAppointmentsActivity, R.layout.user_item, null)

                    val textViewEmail = userDetailsView.findViewById<TextView>(R.id.textViewEmail)
                    val textViewLatitude = userDetailsView.findViewById<TextView>(R.id.textViewLatitude)
                    val textViewLongitude = userDetailsView.findViewById<TextView>(R.id.textViewLongitude)
                    //val textViewUserType = userDetailsView.findViewById<TextView>(R.id.textViewUserType)
                    val textViewName = userDetailsView.findViewById<TextView>(R.id.textViewName)
                    val textViewCity = userDetailsView.findViewById<TextView>(R.id.textViewCity)
                   // val textViewAllergies = userDetailsView.findViewById<TextView>(R.id.textViewAllergies)
                    val textViewSymptoms = userDetailsView.findViewById<TextView>(R.id.textViewSymptoms)
                    val textViewProblems = userDetailsView.findViewById<TextView>(R.id.textViewProblems)
                    val textViewDiagnosis = userDetailsView.findViewById<TextView>(R.id.textViewDiagnosis)
                    val textViewUnmatchedSymptoms = userDetailsView.findViewById<TextView>(R.id.textViewUnmatchedSymptoms)
                    val textViewAge = userDetailsView.findViewById<TextView>(R.id.textViewAge)
                   // val textViewUsername = userDetailsView.findViewById<TextView>(R.id.textViewUsername)
                    val editTextDiseaseResult = userDetailsView.findViewById<EditText>(R.id.editTextDiseaseResult)
                    val buttonSaveDiseaseResult = userDetailsView.findViewById<Button>(R.id.buttonSaveDiseaseResult)

                    textViewEmail.text = snapshot.child("email").value?.toString() ?: "Unknown"
                    textViewLatitude.text = snapshot.child("latitude").value?.toString() ?: "Unknown"
                    textViewLongitude.text = snapshot.child("longitude").value?.toString() ?: "Unknown"
                   // textViewUserType.text = snapshot.child("userType").value?.toString() ?: "Unknown"
                    textViewName.text = snapshot.child("name").value?.toString() ?: "Unknown"
                    textViewCity.text = snapshot.child("city").value?.toString() ?: "Unknown"
                   // textViewAllergies.text = snapshot.child("allergies").value?.toString() ?: "Unknown"
                    textViewSymptoms.text = snapshot.child("symptoms").children.joinToString(", ") { it.value.toString() }
                    textViewProblems.text = snapshot.child("problems").value?.toString() ?: "Unknown"
                    textViewDiagnosis.text = snapshot.child("diseaseResult").value?.toString() ?: "Unknown"
                    textViewUnmatchedSymptoms.text = snapshot.child("unmatchedSymptoms").children.joinToString(", ") { it.value.toString() }
                    textViewAge.text = snapshot.child("age").value?.toString() ?: "Unknown"
                   // textViewUsername.text = snapshot.child("username").value?.toString() ?: "Unknown"

                    editTextDiseaseResult.setText(snapshot.child("diseaseResult").value?.toString() ?: "")

                    if (isDoctor) { // Verific dacă utilizatorul curent este doctor
                        editTextDiseaseResult.visibility = View.VISIBLE
                        buttonSaveDiseaseResult.visibility = View.VISIBLE

                        buttonSaveDiseaseResult.setOnClickListener {
                            val newDiseaseResult = editTextDiseaseResult.text.toString()
                            saveDiseaseResult(userId, newDiseaseResult)
                        }
                    } else {
                        editTextDiseaseResult.visibility = View.GONE
                        buttonSaveDiseaseResult.visibility = View.GONE
                    }

                    userDetailsLayout.addView(userDetailsView)
                    userDetailsLayout.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@DoctorAcceptedAppointmentsActivity, "Failed to load user details.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DoctorAcceptedAppointmentsActivity", "Failed to load user details: ${error.message}")
            }
        })
    }

    private fun saveDiseaseResult(userId: String, newDiseaseResult: String) {
        database.child("users").child(userId).child("diseaseResult").setValue(newDiseaseResult).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("DoctorAcceptedAppointmentsActivity", "Disease result updated successfully for userId: $userId")
                Toast.makeText(this, "Disease result updated successfully.", Toast.LENGTH_SHORT).show()
                userDetailsLayout.visibility = View.GONE // Ascunde layout-ul după salvare


                database.child("users").child(userId).child("hasSavedDiseaseResult").setValue(true)
            } else {
                Log.e("DoctorAcceptedAppointmentsActivity", "Failed to update disease result: ${task.exception?.message}")
                Toast.makeText(this, "Failed to update disease result.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
