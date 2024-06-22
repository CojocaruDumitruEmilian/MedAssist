package com.example.proiectdiploma

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlin.collections.HashMap

class CommonDiseasesActivity : AppCompatActivity() {

    private lateinit var listViewCommonDiseases: ListView
    private lateinit var database: DatabaseReference
    private lateinit var commonDiseasesList: MutableList<CommonDisease>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_diseases)

        listViewCommonDiseases = findViewById(R.id.listViewCommonDiseases)
        database = FirebaseDatabase.getInstance().reference.child("users")
        commonDiseasesList = mutableListOf()

        loadCommonDiseasesByCity()
    }

    private fun loadCommonDiseasesByCity() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cityDiseaseMap = HashMap<String, MutableList<String>>()

                for (userSnapshot in snapshot.children) {
                    val city = userSnapshot.child("city").value.toString()
                    val diseaseResult = userSnapshot.child("diseaseResult").value.toString()

                    Log.d("CommonDiseasesActivity", "City: $city, Disease Result: $diseaseResult")

                    if (city.isNotEmpty() && diseaseResult.isNotEmpty() && diseaseResult != "null") {
                        val diseases = diseaseResult.split("\n")
                            .map { it.split(":")[0].trim() }
                            .filter { it.isNotEmpty() }  // Elimină elementele goale
                        Log.d("CommonDiseasesActivity", "Parsed diseases for city $city: $diseases")

                        if (!cityDiseaseMap.containsKey(city)) {
                            cityDiseaseMap[city] = mutableListOf()
                        }
                        cityDiseaseMap[city]?.addAll(diseases)
                    }
                }

                commonDiseasesList.clear()
                for ((city, diseases) in cityDiseaseMap) {
                    Log.d("CommonDiseasesActivity", "Diseases in city $city: $diseases")
                    if (diseases.isNotEmpty()) {
                        val mostCommonDisease = diseases.groupingBy { it }
                            .eachCount()
                            .maxByOrNull { it.value }?.key
                        Log.d("CommonDiseasesActivity", "City: $city, Most Common Disease: $mostCommonDisease")
                        if (mostCommonDisease != null) {
                            commonDiseasesList.add(CommonDisease(city, mostCommonDisease))
                        }
                    }
                }

                val adapter = CommonDiseaseAdapter(this@CommonDiseasesActivity, commonDiseasesList)
                listViewCommonDiseases.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommonDiseasesActivity", "Failed to load data", error.toException())
                Toast.makeText(this@CommonDiseasesActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

data class CommonDisease(val city: String, val disease: String)
