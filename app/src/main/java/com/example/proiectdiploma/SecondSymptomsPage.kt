package com.example.proiectdiploma

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import java.io.IOException
import java.util.*

class SecondSymptomsPage : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button
    private lateinit var voiceButton: Button // Button pentru recunoaștere vocală
    private lateinit var backButton: Button // Button pentru a reveni la SymptomsPage
    private lateinit var database: DatabaseReference
    private val REQ_CODE_SPEECH_INPUT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_symptoms_page)

        resultTextView = findViewById(R.id.resultTextView)
        inputText = findViewById(R.id.inputText)
        sendButton = findViewById(R.id.sendButton)
        voiceButton = findViewById(R.id.voiceButton) // Inițializare Button pentru recunoaștere vocală
        backButton = findViewById(R.id.backButton) // Inițializare Button pentru a reveni la SymptomsPage

        database = FirebaseDatabase.getInstance().reference

        sendButton.setOnClickListener {
            sendApiRequest()
        }

        // Funcționalitate pentru butonul de recunoaștere vocală
        voiceButton.setOnClickListener {
            promptSpeechInput()
        }

        // Funcționalitate pentru butonul de revenire
        backButton.setOnClickListener {
            val intent = Intent(this, SymptomsPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Spuneți ceva...")

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(applicationContext, "Recunoașterea vocală nu este suportată pe acest dispozitiv.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (result != null && result.isNotEmpty()) {
                        inputText.setText(result[0])
                    }
                }
            }
        }
    }

    private fun sendApiRequest() {
        val client = OkHttpClient()

        val userInput = inputText.text.toString()
        val url = "https://zylalabs.com/api/1990/symptoms+checker+api/1754/symptom+analysis?user_content=$userInput"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer 4382|H361BZxiaQgDgS2PrEbdxULGmmrPws1hYfxniH6r")
            .build()

        Log.d("SecondSymptomsPage", "Request built: $request")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SecondSymptomsPage", "Request failed", e)
                runOnUiThread {
                    resultTextView.text = "Request failed: ${e.message}"
                    resultTextView.setBackgroundResource(R.drawable.response_background) // Setează fundalul textului
                    resultTextView.visibility = TextView.VISIBLE // Fă textul vizibil
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("SecondSymptomsPage", "Response received: $responseData")

                runOnUiThread {
                    if (response.isSuccessful) {
                        resultTextView.text = responseData ?: "No response data"
                        resultTextView.setBackgroundResource(R.drawable.response_background) // Setează fundalul textului
                        resultTextView.visibility = TextView.VISIBLE // Fă textul vizibil
                        // Save unmatched symptoms, user input, and API response to database
                        val symptoms = intent.getStringArrayListExtra("SYMPTOMS_LIST")?.toList() ?: emptyList()
                        saveUnmatchedSymptomsToDatabase(symptoms, userInput, responseData ?: "No response data")
                    } else {
                        resultTextView.text = "Error: ${response.message} - $responseData"
                        resultTextView.setBackgroundResource(R.drawable.response_background) // Setează fundalul textului
                        resultTextView.visibility = TextView.VISIBLE // Fă textul vizibil
                    }
                }
            }
        })
    }

    private fun saveUnmatchedSymptomsToDatabase(symptoms: List<String>, userInput: String, apiResponse: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val resultData = mapOf(
                "symptoms" to symptoms,
                "userInput" to userInput,
                "apiResponse" to apiResponse
            )
            database.child("users").child(userId).child("unmatchedSymptoms").push().setValue(resultData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SecondSymptomsPage", "Unmatched symptoms and API response saved successfully.")
                    } else {
                        Log.e("SecondSymptomsPage", "Failed to save unmatched symptoms and API response", task.exception)
                    }
                }
        }
    }
}
