package com.example.proiectdiploma

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class RateDoctor : AppCompatActivity() {

    private lateinit var doctorNameTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var submitButton: Button
    private lateinit var doctorId: String
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_doctor)

        doctorNameTextView = findViewById(R.id.textViewDoctorName)
        ratingBar = findViewById(R.id.ratingBar)
        submitButton = findViewById(R.id.buttonSubmitRating)

        doctorId = intent.getStringExtra("DOCTOR_ID") ?: run {
            Log.e("RateDoctor", "Doctor ID is null")
            finish()
            return
        }

        val doctorName = intent.getStringExtra("DOCTOR_NAME") ?: "Unknown Doctor"
        database = FirebaseDatabase.getInstance().reference

        doctorNameTextView.text = doctorName

        submitButton.setOnClickListener {
            val rating = ratingBar.rating
            submitRating(doctorId, rating)
        }
    }

    private fun submitRating(doctorId: String, rating: Float) {
        val averageRatingRef = database.child("doctors").child(doctorId).child("averageRating")

        averageRatingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalRating = rating
                var ratingCount = 1

                if (snapshot.exists()) {
                    val currentAverage = snapshot.child("average").getValue(Float::class.java) ?: 0f
                    val currentCount = snapshot.child("count").getValue(Int::class.java) ?: 0

                    totalRating += currentAverage * currentCount
                    ratingCount += currentCount
                }

                val newAverage = totalRating / ratingCount
                val averageData = mapOf("average" to newAverage, "count" to ratingCount)

                averageRatingRef.setValue(averageData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@RateDoctor, "Rating submitted successfully.", Toast.LENGTH_SHORT).show()
                        Log.d("RateDoctor", "Rating submitted successfully.")
                        finish()
                    } else {
                        Toast.makeText(this@RateDoctor, "Failed to submit rating.", Toast.LENGTH_SHORT).show()
                        Log.e("RateDoctor", "Failed to submit rating: ${task.exception?.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RateDoctor", "Failed to read average rating: ${error.message}")
                Toast.makeText(this@RateDoctor, "Failed to read average rating.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
