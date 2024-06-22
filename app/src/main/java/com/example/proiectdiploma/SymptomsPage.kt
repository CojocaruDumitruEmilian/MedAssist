package com.example.proiectdiploma

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.material.button.MaterialButton

class SymptomsPage : AppCompatActivity() {
    private lateinit var resultTextView: TextView
    private lateinit var selectedSymptomsLayout: LinearLayout
    private lateinit var symptomDropdown: Spinner
    private lateinit var symptomAutoComplete: AutoCompleteTextView
    private lateinit var generateButton: Button
    private lateinit var doctorInfoLayout: LinearLayout
    private lateinit var acceptedMessageTextView: TextView
    private lateinit var selectedSymptomsScrollView: ScrollView
    private var selectedSymptomsList = mutableListOf<String>()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_page)

        selectedSymptomsLayout = findViewById(R.id.selectedSymptomsLayout)
        resultTextView = findViewById(R.id.resultTextView)
        symptomDropdown = findViewById(R.id.symptomDropdown)
        symptomAutoComplete = findViewById(R.id.symptomAutoComplete)
        generateButton = findViewById(R.id.generateButton)
        doctorInfoLayout = findViewById(R.id.doctorInfoLayout)
        acceptedMessageTextView = findViewById(R.id.acceptedMessageTextView)
        selectedSymptomsScrollView = findViewById(R.id.selectedSymptomsScrollView)

        // Setăm invizibile layout-urile inițial
        selectedSymptomsLayout.visibility = View.GONE
        doctorInfoLayout.visibility = View.GONE
        resultTextView.visibility = View.GONE

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            userId = user.uid
            Log.d("SymptomsPage", "User ID initialized: $userId")
            checkDoctorAcceptance()
        } else {
            Log.e("SymptomsPage", "User is null.")
        }

        // Initialize dropdown and autocomplete and set up listeners
        val diseasesWithSymptoms = Diseases.diseasesWithSymptoms
        val allSymptoms = diseasesWithSymptoms.flatMap { it.second.toList() }
        val uniqueSymptoms = listOf("Selectați un simptom") + allSymptoms.distinct()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, uniqueSymptoms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        symptomDropdown.adapter = adapter

        symptomDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    val selectedSymptom = parent?.getItemAtPosition(position).toString()
                    if (!selectedSymptomsList.contains(selectedSymptom)) {
                        selectedSymptomsList.add(selectedSymptom)
                        addSymptomButton(selectedSymptom)
                        updateScrollViewVisibility()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val autoCompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, uniqueSymptoms)
        symptomAutoComplete.setAdapter(autoCompleteAdapter)

        symptomAutoComplete.setOnItemClickListener { parent, view, position, id ->
            val selectedSymptom = parent.getItemAtPosition(position).toString()
            if (!selectedSymptomsList.contains(selectedSymptom)) {
                selectedSymptomsList.add(selectedSymptom)
                addSymptomButton(selectedSymptom)
                updateScrollViewVisibility()
            }
            symptomAutoComplete.text.clear() // Clear the input after selection
        }

        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            signOut()
        }

        generateButton.setOnClickListener {
            generateDiseaseResult()
        }

        val navigateToNewPageButton: Button = findViewById(R.id.secondSymptomsPageButton)
        navigateToNewPageButton.setOnClickListener {
            val intent = Intent(this, SecondSymptomsPage::class.java)
            startActivity(intent)
        }
    }

    private fun addSymptomButton(symptom: String) {
        val button = MaterialButton(this, null, android.R.attr.buttonStyleSmall).apply {
            text = symptom
            setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_delete, 0)
            setOnClickListener {
                removeSelectedSymptom(symptom)
            }
        }
        selectedSymptomsLayout.addView(button)
        selectedSymptomsLayout.visibility = View.VISIBLE // Setăm vizibil când adăugăm un simptom
        updateScrollViewVisibility()
    }

    private fun updateSelectedSymptomsLayout() {
        selectedSymptomsLayout.removeAllViews()
        for (i in selectedSymptomsList.indices) {
            val button = MaterialButton(this, null, android.R.attr.buttonStyleSmall).apply {
                text = selectedSymptomsList[i]
                setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0)
                setOnClickListener {
                    removeSelectedSymptom(selectedSymptomsList[i])
                }
            }
            selectedSymptomsLayout.addView(button)
        }
        updateScrollViewVisibility()
    }

    private fun updateScrollViewVisibility() {
        if (selectedSymptomsList.isEmpty()) {
            selectedSymptomsLayout.visibility = View.GONE // Ascunde layout-ul dacă lista este goală
        } else {
            selectedSymptomsLayout.visibility = View.VISIBLE // Arată layout-ul dacă lista nu este goală
        }

        if (selectedSymptomsList.size > 3) {
            selectedSymptomsScrollView.layoutParams.height = 300 // Set max height
        } else {
            selectedSymptomsScrollView.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        }
    }

    private fun generateDiseaseResult() {
        val diseasesWithSymptoms = Diseases.diseasesWithSymptoms
        val matchingResults = mutableListOf<Pair<String, Int>>()

        for (disease in diseasesWithSymptoms) {
            val diseaseName = disease.first
            val symptoms = disease.second
            val matchingSymptomsCount = selectedSymptomsList.count { symptoms.contains(it) }
            val matchingPercentage = (matchingSymptomsCount.toDouble() / symptoms.size) * 100
            if (matchingPercentage > 0) {
                matchingResults.add(Pair(diseaseName, matchingPercentage.toInt()))
            }
        }

        val resultText = StringBuilder()
        for (result in matchingResults) {
            resultText.append("${result.first}: ${result.second}%\n")
        }

        if (matchingResults.isEmpty()) {
            resultTextView.text = "Nicio boală nu se potrivește cu simptomele selectate."
        } else {
            resultTextView.text = resultText.toString()
            saveResultsToDatabase(resultText.toString())
            val highestMatchingDisease = matchingResults.maxByOrNull { it.second }
            if (highestMatchingDisease != null) {
                showSpecialistDoctors(highestMatchingDisease.first)
            }
        }

        resultTextView.visibility = View.VISIBLE
        resultTextView.setBackgroundColor(resources.getColor(android.R.color.white, null)) // Set background to white
        resultTextView.setTextColor(resources.getColor(android.R.color.black, null)) // Set text color to black
    }

    private fun saveResultsToDatabase(diseaseResult: String) {
        userId?.let {
            Log.d("SymptomsPage", "Saving results to database for user: $it")
            val userUpdates = mapOf(
                "symptoms" to selectedSymptomsList,
                "diseaseResult" to diseaseResult
            )
            database.child("users").child(it).updateChildren(userUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SymptomsPage", "User symptoms and disease result updated successfully.")
                        deleteUnmatchedSymptoms()
                    } else {
                        Log.e("SymptomsPage", "Failed to update user symptoms and disease result: ${task.exception?.message}")
                    }
                }
        } ?: run {
            Log.e("SymptomsPage", "User ID is null. Cannot save results to database.")
            Toast.makeText(this, "User ID is not initialized.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteUnmatchedSymptoms() {
        userId?.let {
            database.child("users").child(it).child("unmatchedSymptoms").removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SymptomsPage", "Unmatched symptoms deleted successfully.")
                    } else {
                        Log.e("SymptomsPage", "Failed to delete unmatched symptoms: ${task.exception?.message}")
                    }
                }
        } ?: run {
            Log.e("SymptomsPage", "User ID is null. Cannot delete unmatched symptoms.")
            Toast.makeText(this, "User ID is not initialized.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSpecialistDoctors(disease: String) {
        doctorInfoLayout.removeAllViews()
        doctorInfoLayout.visibility = View.VISIBLE // Setăm vizibil
        doctorInfoLayout.setBackgroundColor(resources.getColor(android.R.color.white, null)) // Set background to white
        val specialty = when (disease) {
            "Hypertensive disease" -> "Cardiologist"
            "Coronavirus disease 2019" -> "Pulmonologist"
            "Depression mental" -> "Neurologist"
            else -> null
        }

        if (specialty != null) {
            Log.d("SymptomsPage", "Looking for doctors with specialty: $specialty")
            database.child("doctors").orderByChild("specialization").equalTo(specialty)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val doctorsList = mutableListOf<Doctor>()

                            for (doctorSnapshot in snapshot.children) {
                                val doctorId = doctorSnapshot.key ?: continue
                                val doctorName = doctorSnapshot.child("firstName").value.toString() + " " +
                                        doctorSnapshot.child("lastName").value.toString()
                                val doctorRating = doctorSnapshot.child("averageRating").child("average").getValue(Float::class.java) ?: 0f
                                doctorsList.add(Doctor(doctorId, doctorName, doctorRating))
                            }

                            // Sort doctors by rating
                            doctorsList.sortByDescending { it.rating }

                            // Add sorted doctors to the layout
                            for (doctor in doctorsList) {
                                Log.d("SymptomsPage", "Found doctor: ${doctor.name}")
                                val textView = TextView(this@SymptomsPage).apply {
                                    text = "Specialist $specialty: ${doctor.name} - Rating: ${doctor.rating}"
                                    textSize = 16f
                                    setOnClickListener {
                                        val intent = Intent(this@SymptomsPage, DoctorDetailsActivity::class.java).apply {
                                            putExtra("DOCTOR_ID", doctor.id)
                                            putExtra("DOCTOR_NAME", doctor.name)
                                        }
                                        startActivity(intent)
                                    }
                                }
                                doctorInfoLayout.addView(textView)
                            }
                        } else {
                            Log.d("SymptomsPage", "No doctors found with specialty: $specialty")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SymptomsPage", "Failed to load doctors: ${error.message}")
                    }
                })
        }
    }

    data class Doctor(val id: String, val name: String, val rating: Float)

    private fun saveDoctorSelectionToDatabase(doctorId: String, doctorName: String, doctorSpecialty: String) {
        userId?.let {
            Log.d("SymptomsPage", "Saving doctor selection to database for user: $it")
            val doctorOptionRequest = mapOf(
                "doctorId" to doctorId,
                "doctorName" to doctorName,
                "doctorSpecialty" to doctorSpecialty
            )
            database.child("users").child(it).child("doctorOptionRequest").setValue(doctorOptionRequest)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SymptomsPage", "Doctor option request saved successfully.")
                        // Navigate to DoctorDetailsActivity
                        val intent = Intent(this, DoctorDetailsActivity::class.java).apply {
                            putExtra("doctorId", doctorId)
                            putExtra("doctorName", doctorName)
                            putExtra("doctorSpecialty", doctorSpecialty)
                        }
                        startActivity(intent)
                    } else {
                        Log.e("SymptomsPage", "Failed to save doctor option request: ${task.exception?.message}")
                    }
                }
        } ?: run {
            Log.e("SymptomsPage", "User ID is null. Cannot save doctor selection to database.")
            Toast.makeText(this, "User ID is not initialized.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkDoctorAcceptance() {
        userId?.let {
            database.child("users").child(it).child("doctorOptionRequest").child("accepted").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value == true) {
                        showAcceptedMessage()
                    } else {
                        showSymptomsSelection()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SymptomsPage", "Failed to check doctor acceptance: ${error.message}")
                }
            })
        } ?: run {
            Log.e("SymptomsPage", "User ID is null. Cannot check doctor acceptance.")
            Toast.makeText(this, "User ID is not initialized.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAcceptedMessage() {
        acceptedMessageTextView.visibility = View.VISIBLE
        selectedSymptomsLayout.visibility = View.GONE
        symptomDropdown.visibility = View.GONE
        symptomAutoComplete.visibility = View.GONE
        generateButton.visibility = View.GONE
        doctorInfoLayout.visibility = View.GONE
        resultTextView.visibility = View.GONE
    }

    private fun showSymptomsSelection() {
        acceptedMessageTextView.visibility = View.GONE
        selectedSymptomsLayout.visibility = View.VISIBLE
        symptomDropdown.visibility = View.VISIBLE
        symptomAutoComplete.visibility = View.VISIBLE
        generateButton.visibility = View.VISIBLE
        doctorInfoLayout.visibility = View.GONE
        resultTextView.visibility = View.GONE
    }

    private fun removeSelectedSymptom(symptom: String) {
        val trimmedSymptom = symptom.trim()
        if (selectedSymptomsList.contains(trimmedSymptom)) {
            selectedSymptomsList.remove(trimmedSymptom)
            Log.d("SymptomsPage", "Removed: true")
        } else {
            Log.d("SymptomsPage", "Removed: false")
        }
        updateSelectedSymptomsLayout()
        if (selectedSymptomsList.isEmpty()) {
            selectedSymptomsLayout.visibility = View.GONE // Ascundem layout-ul dacă lista este goală
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}
