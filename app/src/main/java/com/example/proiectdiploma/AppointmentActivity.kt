package com.example.proiectdiploma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AppointmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        val doctorFirstName = intent.getStringExtra("DoctorFirstName")
        val doctorLastName = intent.getStringExtra("DoctorLastName")
        val doctorId = intent.getStringExtra("DoctorId")

        val doctorNameTextView: TextView = findViewById(R.id.doctorsNameTextView)
        val datePicker: DatePicker = findViewById(R.id.datePicker)
        val timePicker: TimePicker = findViewById(R.id.timePicker)
        val confirmAppointmentButton: Button = findViewById(R.id.confirmAppointmentButton)

        doctorNameTextView.text = "Doctor's Name: $doctorFirstName $doctorLastName"

        confirmAppointmentButton.setOnClickListener {
            val selectedDate = "${datePicker.year}-${datePicker.month + 1}-${datePicker.dayOfMonth}"
            val selectedTime = "${timePicker.hour}:${timePicker.minute}"

            val appointmentDetailsText = "Appointment with $doctorFirstName $doctorLastName on $selectedDate at $selectedTime"

            // Afișează detalii într-un TextView sau în alt mod dorit
            val appointmentDetailsTextView: TextView = findViewById(R.id.appointmentDetailsTextView)
            appointmentDetailsTextView.text = appointmentDetailsText

            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                val uid = user.uid
                val userName = user.displayName ?: "User"

                // Salvează detaliile programării în lista utilizatorului
                val appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments/$uid")
                val appointmentId = appointmentsRef.push().key
                appointmentId?.let {
                    val appointmentDetails = "Appointment with $doctorFirstName $doctorLastName on $selectedDate at $selectedTime"
                    appointmentsRef.child(it).setValue(appointmentDetails)
                }

                // Salvează detaliile programării în lista doctorului
                if (doctorId != null) {
                    val doctorAppointmentsRef = FirebaseDatabase.getInstance().getReference("doctors/$doctorId/pending")
                    val doctorAppointmentId = doctorAppointmentsRef.push().key
                    doctorAppointmentId?.let {
                        val doctorAppointmentDetails = mapOf(
                            "userName" to userName,
                            "userId" to uid,
                            "date" to selectedDate,
                            "time" to selectedTime
                        )
                        doctorAppointmentsRef.child(it).setValue(doctorAppointmentDetails)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Appointment scheduled successfully.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Failed to schedule appointment: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            }

            val intent = Intent(this, UserAppointmentsActivity::class.java)
            intent.putExtra("DoctorFirstName", doctorFirstName)
            intent.putExtra("DoctorLastName", doctorLastName)
            intent.putExtra("SelectedDate", selectedDate)
            intent.putExtra("SelectedTime", selectedTime)
            startActivity(intent)
            finish()
        }
    }
}
