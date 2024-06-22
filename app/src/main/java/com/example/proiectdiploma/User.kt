package com.example.proiectdiploma

data class User(
    val userId: String = "",
    val userType: String = "",
    val email: String = "",
    val name: String = "",
    val city: String = "",
    val age: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val allergies: String = "",
    val symptoms: List<String> = emptyList(),
    val unmatchedSymptoms: Map<String, UnmatchedSymptom>? = null,
    val diagnosis: Map<String, Diagnosis>? = null,
    val diseaseResult: String = "" ,// Updated to match your data structure,

    val username: String = ""
)

data class UnmatchedSymptom(
    val apiResponse: String = "",
    val userInput: String = ""
)

data class Diagnosis(
    val diseaseResult: String = ""
)
