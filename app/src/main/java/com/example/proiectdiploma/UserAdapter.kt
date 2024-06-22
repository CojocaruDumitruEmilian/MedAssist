package com.example.proiectdiploma

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class UserAdapter(context: Context, private val users: List<User>) :
    ArrayAdapter<User>(context, 0, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)

        val user = getItem(position)

        val textViewEmail = view.findViewById<TextView>(R.id.textViewEmail)
        //val textViewUserType = view.findViewById<TextView>(R.id.textViewUserType)
        val textViewSymptoms = view.findViewById<TextView>(R.id.textViewSymptoms)
        val textViewName = view.findViewById<TextView>(R.id.textViewName)
        val textViewCity = view.findViewById<TextView>(R.id.textViewCity)
       // val textViewAllergies = view.findViewById<TextView>(R.id.textViewAllergies)
        val textViewDiagnosis = view.findViewById<TextView>(R.id.textViewDiagnosis)
        val textViewUnmatchedSymptoms = view.findViewById<TextView>(R.id.textViewUnmatchedSymptoms)
        val textViewLatitude = view.findViewById<TextView>(R.id.textViewLatitude)
        val textViewLongitude = view.findViewById<TextView>(R.id.textViewLongitude)
        val textViewUAge = view.findViewById<TextView>(R.id.textViewAge)

        textViewEmail.text = "Email: ${user?.email}"
       // textViewUserType.text = "User Type: ${user?.userType}"
        textViewSymptoms.text = "Symptoms: ${user?.symptoms?.joinToString(", ") ?: "No symptoms"}"
        textViewName.text = "Name: ${user?.name}"
        textViewCity.text = "City: ${user?.city}"
       // textViewAllergies.text = "Allergies: ${user?.allergies}"
        textViewDiagnosis.text = "Diagnosis: ${user?.diseaseResult}"
        textViewUnmatchedSymptoms.text = "Unmatched Symptoms: ${user?.unmatchedSymptoms?.values?.joinToString(", ") { "${it.userInput}: ${it.apiResponse}" } ?: "No unmatched symptoms"}"
        textViewLatitude.text = "Latitude: ${user?.latitude}"
        textViewLongitude.text = "Longitude: ${user?.longitude}"
        textViewUAge.text="Age: ${user?.age}"

        return view
    }
}
