package com.example.proiectdiploma

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class DoctorDetailsActivity : AppCompatActivity() {

    private lateinit var doctorNameTextView: TextView
    private lateinit var doctorRatingTextView: TextView // TextView pentru afișarea rating-ului
    private lateinit var doctorRatingBar: RatingBar // RatingBar pentru afișarea rating-ului
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var nextAvailableSlotTextView: TextView
    private lateinit var scheduleButton: Button
    private lateinit var backButton: Button // Button pentru a reveni la SymptomsPage
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null
    private var doctorId: String? = null
    private var doctorName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_details)

        doctorNameTextView = findViewById(R.id.doctorNameTextView)
        doctorRatingTextView = findViewById(R.id.doctorRatingTextView) // Inițializare TextView pentru rating
        doctorRatingBar = findViewById(R.id.doctorRatingBar) // Inițializare RatingBar pentru rating
        dateEditText = findViewById(R.id.dateEditText)
        timeEditText = findViewById(R.id.timeEditText)
        nextAvailableSlotTextView = findViewById(R.id.nextAvailableSlotTextView)
        scheduleButton = findViewById(R.id.scheduleButton)
        backButton = findViewById(R.id.backButton) // Inițializare Button pentru a reveni la SymptomsPage

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        doctorId = intent.getStringExtra("DOCTOR_ID")
        doctorName = intent.getStringExtra("DOCTOR_NAME")

        doctorNameTextView.text = doctorName

        val user = auth.currentUser
        if (user != null) {
            userId = user.uid
        } else {
            Log.e("DoctorDetailsActivity", "User not logged in.")
            finish()
        }

        // Obține rating-ul mediu al doctorului
        getDoctorRating()

        // Găsește următorul interval disponibil pentru doctor
        findNextAvailableSlot()

        scheduleButton.setOnClickListener {
            scheduleAppointment()
        }

        // Funcționalitate pentru butonul de revenire
        backButton.setOnClickListener {
            val intent = Intent(this, SymptomsPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getDoctorRating() {
        doctorId?.let {
            database.child("doctors").child(it).child("averageRating").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val averageRating = snapshot.child("average").getValue(Float::class.java)
                        doctorRatingTextView.text = "Rating: ${averageRating ?: "N/A"}"
                        doctorRatingBar.rating = averageRating ?: 0f // Setează rating-ul în RatingBar
                    } else {
                        doctorRatingTextView.text = "Rating: N/A"
                        doctorRatingBar.rating = 0f // Setează rating-ul în RatingBar la 0 dacă nu există
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DoctorDetailsActivity", "Failed to get doctor rating: ${error.message}")
                }
            })
        }
    }

    private fun findNextAvailableSlot() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val currentDate = dateFormat.format(calendar.time)
        val currentTime = timeFormat.format(calendar.time)

        doctorId?.let {
            database.child("appointments")
                .orderByChild("doctorId")
                .equalTo(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val occupiedSlots = mutableListOf<String>()

                        for (appointmentSnapshot in snapshot.children) {
                            val date = appointmentSnapshot.child("date").getValue(String::class.java)
                            val time = appointmentSnapshot.child("time").getValue(String::class.java)
                            if (date != null && time != null) {
                                occupiedSlots.add("$date $time")
                            }
                        }

                        var nextAvailableDateTime: String? = null
                        while (nextAvailableDateTime == null) {
                            val date = dateFormat.format(calendar.time)
                            val time = timeFormat.format(calendar.time)
                            val dateTime = "$date $time"
                            if (dateTime > "$currentDate $currentTime" && !occupiedSlots.contains(dateTime)) {
                                nextAvailableDateTime = dateTime
                            }
                            calendar.add(Calendar.MINUTE, 30) // Mărește intervalul cu 30 de minute
                        }

                        nextAvailableSlotTextView.text = "Următorul interval disponibil: $nextAvailableDateTime"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DoctorDetailsActivity", "Failed to get appointments: ${error.message}")
                    }
                })
        }
    }

    private fun scheduleAppointment() {
        val date = dateEditText.text.toString()
        val time = timeEditText.text.toString()

        if (doctorId == null || doctorName == null) {
            Toast.makeText(this, "Doctor information is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        if (date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please enter date and time.", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificăm dacă doctorul are deja o programare la acea dată și oră
        database.child("appointments")
            .orderByChild("doctorId")
            .equalTo(doctorId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var appointmentExists = false
                    for (appointmentSnapshot in snapshot.children) {
                        val existingDate = appointmentSnapshot.child("date").getValue(String::class.java)
                        val existingTime = appointmentSnapshot.child("time").getValue(String::class.java)
                        if (existingDate == date && existingTime == time) {
                            appointmentExists = true
                            break
                        }
                    }

                    if (appointmentExists) {
                        Toast.makeText(this@DoctorDetailsActivity, "Doctorul are deja o programare în acest interval.", Toast.LENGTH_SHORT).show()
                    } else {
                        createAppointment(date, time)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DoctorDetailsActivity", "Failed to check existing appointments: ${error.message}")
                }
            })
    }

    private fun createAppointment(date: String, time: String) {
        userId?.let { userId ->
            database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").value.toString()
                    val userEmail = snapshot.child("email").value.toString()
                    val userCity = snapshot.child("city").value.toString()
                    val userAge = snapshot.child("age").value.toString()
                    val userAllergies = snapshot.child("allergies").value.toString()
                    val userDiseaseResult = snapshot.child("diseaseResult").value.toString()
                    val userSymptoms = snapshot.child("symptoms").children.joinToString { it.value.toString() }

                    val appointmentKey = database.child("appointments").push().key
                    if (appointmentKey == null) {
                        Log.e("DoctorDetailsActivity", "Failed to generate appointment key.")
                        return
                    }

                    val appointment = mapOf(
                        "userId" to userId,
                        "userName" to userName,
                        "userEmail" to userEmail,
                        "userCity" to userCity,
                        "userAge" to userAge,
                        "userAllergies" to userAllergies,
                        "userDiseaseResult" to userDiseaseResult,
                        "userSymptoms" to userSymptoms,
                        "doctorId" to doctorId,
                        "doctorName" to doctorName,
                        "date" to date,
                        "time" to time,
                        "status" to "pending"
                    )

                    val updates = hashMapOf<String, Any>(
                        "appointments/$appointmentKey" to appointment,
                        "doctors/$doctorId/userPending/$appointmentKey" to appointment
                    )

                    database.updateChildren(updates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@DoctorDetailsActivity, "Appointment scheduled successfully.", Toast.LENGTH_SHORT).show()
                            findNextAvailableSlot() // Actualizează următorul interval disponibil
                        } else {
                            Toast.makeText(this@DoctorDetailsActivity, "Failed to schedule appointment.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DoctorDetailsActivity", "Failed to get user details: ${error.message}")
                }
            })
        }
    }
}
