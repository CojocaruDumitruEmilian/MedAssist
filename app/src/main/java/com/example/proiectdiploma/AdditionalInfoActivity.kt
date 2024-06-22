package com.example.proiectdiploma

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import java.util.*

class AdditionalInfoActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var radioGroupAllergies: RadioGroup
    private lateinit var radioGroupSmoking: RadioGroup
    private lateinit var radioGroupHereditaryProblems: RadioGroup
    private lateinit var btnRegister: Button
    private lateinit var btnGetLocation: Button
    private lateinit var spinnerCities: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentCity: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additional_info)

        etName = findViewById(R.id.et_name)
        etAge = findViewById(R.id.et_age)
        radioGroupAllergies = findViewById(R.id.radio_group_allergies)
        radioGroupSmoking = findViewById(R.id.radio_group_smoking)
        radioGroupHereditaryProblems = findViewById(R.id.radio_group_hereditary_problems)
        btnRegister = findViewById(R.id.btn_register)
        btnGetLocation = findViewById(R.id.btn_get_location)
        spinnerCities = findViewById(R.id.spinner_cities)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Populate the spinner with a list of Romanian cities
        val cities = arrayOf("Bucharest", "Cluj-Napoca", "Timișoara", "Iași", "Constanța", "Craiova", "Brașov", "Galați", "Ploiești", "Oradea", "Brăila", "Arad", "Pitești", "Sibiu", "Bacău", "Târgu Mureș", "Baia Mare", "Buzău", "Botoșani", "Satu Mare", "Râmnicu Vâlcea", "Drobeta-Turnu Severin", "Suceava", "Piatra Neamț", "Reșița", "Focșani", "Bistrița", "Tulcea", "Târgoviște", "Bârlad", "Alba Iulia", "Deva", "Zalău", "Sfântu Gheorghe", "Hunedoara", "Giurgiu", "Roman", "Câmpina", "Câmpulung", "Sighetu Marmației", "Făgăraș", "Miercurea Ciuc", "Turda", "Mediaș", "Lugoj", "Slobozia", "Tecuci", "Odorheiu Secuiesc", "Petroșani", "Motru", "Caracal", "Călărași", "Slatina", "Sebeș", "Pașcani", "Roșiorii de Vede", "Turnu Măgurele", "Vaslui", "Vulcan", "Câmpulung Moldovenesc", "Carei", "Codlea", "Târnăveni", "Rădăuți", "Năvodari", "Fălticeni", "Blaj", "Gheorgheni", "Borșa", "Mangalia", "Rovinari", "Aiud", "Petrila", "Curtea de Argeș", "Hațeg", "Uricani", "Comănești", "Buftea", "Mioveni", "Cugir", "Dorohoi", "Oltenița", "Breaza", "Târgu Neamț", "Drăgășani", "Salonta", "Fieni", "Boldești-Scăeni", "Băicoi", "Țăndărei", "Săcele", "Popești-Leordeni", "Voluntari", "Pantelimon", "Bragadiru", "Chitila", "Otopeni", "Balș", "Cisnădie", "Ovidiu", "Chitila", "Buftea", "Voluntari")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCities.adapter = adapter

        btnGetLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                getLastLocation()
            }
        }

        btnRegister.setOnClickListener {
            Log.d("AdditionalInfoActivity", "Register button clicked")

            val name = etName.text.toString().trim()
            val age = etAge.text.toString().trim()
            val allergies = when (radioGroupAllergies.checkedRadioButtonId) {
                R.id.radio_allergies_yes -> "Yes"
                R.id.radio_allergies_no -> "No"
                else -> ""
            }
            val smoking = when (radioGroupSmoking.checkedRadioButtonId) {
                R.id.radio_smoking_yes -> "Yes"
                R.id.radio_smoking_no -> "No"
                else -> ""
            }
            val hereditaryProblems = when (radioGroupHereditaryProblems.checkedRadioButtonId) {
                R.id.radio_hereditary_problems_yes -> "Yes"
                R.id.radio_hereditary_problems_no -> "No"
                else -> ""
            }
            val selectedCity = currentCity.ifEmpty { spinnerCities.selectedItem.toString() }

            if (name.isEmpty() || age.isEmpty() || allergies.isEmpty() || smoking.isEmpty() || hereditaryProblems.isEmpty() || selectedCity.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val email = user.email ?: "Unknown"
                val userInfo = mapOf(
                    "name" to name,
                    "age" to age,
                    "allergies" to allergies,
                    "smoking" to smoking,
                    "hereditaryProblems" to hereditaryProblems,
                    "email" to email,
                    "city" to selectedCity
                )

                Log.d("AdditionalInfoActivity", "Saving user info: $userInfo")

                database.child("users").child(userId).setValue(userInfo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Registration complete", Toast.LENGTH_LONG).show()
                            Log.d("AdditionalInfoActivity", "Data saved successfully")
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.e(ContentValues.TAG, "Failed to save user info", task.exception)
                            Toast.makeText(this, "Failed to save user info", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Log.e("AdditionalInfoActivity", "User is not logged in")
                Toast.makeText(this, "User is not logged in", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnCompleteListener(this) { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    Log.d("AdditionalInfoActivity", "Current location: ${location.latitude}, ${location.longitude}")
                    getCityNameFromLocation(location.latitude, location.longitude)
                } else {
                    Log.w("AdditionalInfoActivity", "Failed to get location.", task.exception)
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun getCityNameFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                currentCity = address.locality ?: "Unknown City"
                Log.d("AdditionalInfoActivity", "Detected city: $currentCity")
                showLocationConfirmationDialog()
            } else {
                Log.w("AdditionalInfoActivity", "No address found for location.")
                Toast.makeText(this, "No address found for location", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLocationConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Location")
        builder.setMessage("Detected city: $currentCity. Do you accept?")
        builder.setPositiveButton("Accept") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(this, "City accepted: $currentCity", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("Refuse") { dialog, _ ->
            currentCity = ""
            dialog.dismiss()
            Toast.makeText(this, "Please select your city manually.", Toast.LENGTH_LONG).show()
        }
        builder.create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
