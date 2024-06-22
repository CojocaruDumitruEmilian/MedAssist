package com.example.proiectdiploma

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewRegister: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioUser: RadioButton
    private lateinit var radioDoctor: RadioButton
    private lateinit var radioAdmin: RadioButton
    private lateinit var database: DatabaseReference
    private var isFirstLogin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonLogin = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressBar)
        textViewRegister = findViewById(R.id.registerNow)
        radioGroup = findViewById(R.id.radioGroup)
        radioUser = findViewById(R.id.radioUser)
        radioDoctor = findViewById(R.id.radioDoctor)
        radioAdmin = findViewById(R.id.radioAdmin)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("LoginActivity", "signInWithEmail:success, user: ${user?.email}")

                        user?.let {
                            checkUserTypeAndProceed(it.uid, email)
                        }
                    } else {
                        Log.e("LoginActivity", "Authentication failed", task.exception)
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        textViewRegister.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkUserTypeAndProceed(userId: String, email: String) {
        when {
            radioAdmin.isChecked -> {
                if (email == "admin@gmail.com") {
                    val intent = Intent(this@Login, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Login, "Role mismatch. Please select the correct role.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
            radioDoctor.isChecked -> {
                database.child("doctors").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val intent = Intent(this@Login, DoctorHomePage::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Login, "Role mismatch. Please select the correct role.", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("LoginActivity", "Failed to get doctor type", error.toException())
                        Toast.makeText(this@Login, "Failed to get doctor type", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            radioUser.isChecked -> {
                if (email != "admin@gmail.com") {
                    database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                checkIfUserAccepted(userId)
                            } else {
                                Toast.makeText(this@Login, "Role mismatch. Please select the correct role.", Toast.LENGTH_SHORT).show()
                                auth.signOut()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("LoginActivity", "Failed to get user type", error.toException())
                            Toast.makeText(this@Login, "Failed to get user type", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this@Login, "Role mismatch. Please select the correct role.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
            else -> {
                Toast.makeText(this, "Please select a role.", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
        }
    }

    private fun checkIfUserAccepted(userId: String) {
        Log.d("LoginActivity", "Checking if user is accepted: $userId")
        database.child("users").child(userId).child("notifications").orderByChild("status").equalTo("accepted")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var accepted = false
                    var doctorId = ""

                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val status = child.child("status").value.toString()
                            Log.d("LoginActivity", "Notification status: $status")

                            if (status == "accepted") {
                                accepted = true
                                doctorId = child.child("doctorId").value.toString()
                                Log.d("LoginActivity", "Doctor ID: $doctorId")
                                break
                            }
                        }
                    } else {
                        Log.d("LoginActivity", "No notifications found for user: $userId")
                    }

                    if (accepted) {
                        Log.d("LoginActivity", "User accepted, checking if disease result saved")

                        database.child("doctors").child(doctorId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(doctorSnapshot: DataSnapshot) {
                                val firstName = doctorSnapshot.child("firstName").value?.toString() ?: "Unknown"
                                val lastName = doctorSnapshot.child("lastName").value?.toString() ?: "Doctor"
                                val doctorName = "$firstName $lastName"

                                Log.d("LoginActivity", "Doctor Name: $doctorName")

                                database.child("users").child(userId).child("hasSavedDiseaseResult").addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists() && snapshot.value == true) {
                                            Log.d("LoginActivity", "Disease result saved, redirecting to RateDoctorActivity")
                                            val intent = Intent(this@Login, RateDoctor::class.java).apply {
                                                putExtra("DOCTOR_ID", doctorId)
                                                putExtra("DOCTOR_NAME", doctorName)
                                            }
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Log.d("LoginActivity", "Disease result not saved, redirecting to UserNotificationsActivity")
                                            val intent = Intent(this@Login, UserNotificationsActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("LoginActivity", "Failed to check disease result: ${error.message}")
                                        Toast.makeText(this@Login, "Failed to check disease result: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("LoginActivity", "Failed to get doctor name: ${error.message}")
                            }
                        })
                    } else {
                        if (isFirstLogin) {
                            Log.d("LoginActivity", "User not accepted, redirecting to SymptomsPage")
                            val intent = Intent(this@Login, SymptomsPage::class.java)
                            startActivity(intent)
                            finish()
                            isFirstLogin = false
                        } else {
                            Toast.makeText(this@Login, "Role mismatch. Please select the correct role.", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LoginActivity", "Failed to check doctor acceptance: ${error.message}")
                    Toast.makeText(this@Login, "Failed to check doctor acceptance: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // No need to handle location permissions here anymore
    }
}
