// DoctorInfoActivity.kt
package com.example.proiectdiploma

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DoctorInfoActivity : AppCompatActivity() {

    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var spinnerSpecialization: Spinner
    private lateinit var buttonDone: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_info)

        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextLastName = findViewById(R.id.editTextLastName)
        spinnerSpecialization = findViewById(R.id.spinnerSpecialization)
        buttonDone = findViewById(R.id.buttonDone)

        buttonDone.setOnClickListener {
            saveDoctorInfoToDatabase()
        }
    }

    private fun saveDoctorInfoToDatabase() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val firstName = editTextFirstName.text.toString()
            val lastName = editTextLastName.text.toString()
            val specialization = spinnerSpecialization.selectedItem.toString()

            val doctorInfo = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "specialization" to specialization,
                "email" to user.email
            )

            FirebaseDatabase.getInstance().reference.child("doctors").child(userId).setValue(doctorInfo)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Afișează un mesaj de confirmare și redirecționează către login
                        Toast.makeText(this, "Doctor info saved successfully.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Afișează un mesaj de eroare
                        Toast.makeText(this, "Failed to save doctor info: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
