package com.example.proiectdiploma

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonReg: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressbar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var radioUser: RadioButton
    private lateinit var radioDoctor: RadioButton
    private lateinit var radioGroup: RadioGroup

    public override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        buttonReg = findViewById(R.id.btn_register)
        progressbar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)

        radioUser = findViewById(R.id.radioUser)
        radioDoctor = findViewById(R.id.radioDoctor)
        radioGroup = findViewById(R.id.radioGroup)

        textView.setOnClickListener {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        buttonReg.setOnClickListener {
            progressbar.visibility = View.VISIBLE
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_LONG).show()
                progressbar.visibility = View.GONE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_LONG).show()
                progressbar.visibility = View.GONE
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                progressbar.visibility = View.GONE
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                progressbar.visibility = View.GONE
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressbar.visibility = View.GONE

                    if (task.isSuccessful) {
                        if (radioDoctor.isChecked) {
                            val intent = Intent(this, DoctorInfoActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this, AdditionalInfoActivity::class.java)
                            startActivity(intent)
                        }
                        finish()
                    } else {
                        val errorMessage = task.exception?.localizedMessage ?: "Authentication failed."
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e(ContentValues.TAG, "Authentication failed", task.exception)
                    }
                }
        }
    }
}
