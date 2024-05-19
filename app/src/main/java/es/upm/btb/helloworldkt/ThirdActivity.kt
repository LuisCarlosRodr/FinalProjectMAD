package es.upm.btb.helloworldkt

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import es.upm.btb.helloworldkt.R
import es.upm.btb.helloworldkt.persistence.retrofit.IOpenWeather
import es.upm.btb.helloworldkt.persistence.retrofit.WeatherAdapter
import es.upm.btb.helloworldkt.data.WeatherData
import es.upm.btb.helloworldkt.persistence.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ThirdActivity : AppCompatActivity() {
    private val TAG = "ThirdActivityRegister"

    lateinit var database: AppDatabase
    private lateinit var weatherService: IOpenWeather
    private lateinit var weatherAdapter: WeatherAdapter



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        // Toast adicional para la tercera actividad
        Toast.makeText(this, "Has abierto la tercera actividad", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "Register. The third activity has being created.");


    // Get coordinates for selected item. Set default ones if not obtained.
        val timestamp = intent.getLongExtra("timestamp", 0)
        val latitud = intent.getDoubleExtra("latitude", 40.389893)
        val longitud = intent.getDoubleExtra("longitude", -3.627748)

        // Shared prefs. Check if the user identifier is already saved
        val userIdentifier = getUserIdentifier()
        if (userIdentifier == null) {
            Toast.makeText(this, "User ID not set set. Request will not work", Toast.LENGTH_LONG).show()
        }



        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_map -> {
                    val intent = Intent(this, OpenStreetMapActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_list -> {
                    val intent = Intent(this, SecondActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        Log.d(TAG, "Latitude: $latitud, Longitude: $longitud")

        // Find the TextView and set the coordinates
        val coordinatesTextView: TextView = findViewById(R.id.coordinatesTextView)
        coordinatesTextView.text = "Latitud: $latitud, Longitud: $longitud"
        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { dialog, which ->
                    database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "coordinates").build()
                    lifecycleScope.launch(Dispatchers.IO) {
                        Log.d(TAG, "Number of items in database before delete "+database.locationDao().getCount()+".");
                        database.locationDao().deleteLocationByTimestamp(timestamp)
                        Log.d(TAG, "Number of items in database after delete "+database.locationDao().getCount()+".");
                        withContext(Dispatchers.Main) {
                            finish()
                        }
                    }
                    Toast.makeText(this, "Has borrado un dato de la lista", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .show()
        }


        initRetrofit()
        // Configurar RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewWeather)
        recyclerView.layoutManager = LinearLayoutManager(this)
        weatherAdapter = WeatherAdapter(emptyList())
        recyclerView.adapter = weatherAdapter


        val latitude = latitud
        val longitude = longitud
        val apiKey = "cae696030dc7236f1beb504056a846eb"
        requestWeatherData(latitude, longitude, apiKey)
        // Add item to Firebase realtime database
        val addReportButton: Button = findViewById(R.id.addReportButton)
        val editTextReport: EditText = findViewById(R.id.editTextReport)
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        addReportButton.setOnClickListener {
            val reportText = editTextReport.text.toString().trim()
            if (reportText.isNotEmpty() && userId != null) {
                val report = mapOf(
                    "userId" to userId,
                    "timestamp" to timestamp,
                    "report" to reportText,
                    "latitude" to latitude,
                    "longitude" to longitude
                )
                addReportToDatabase(report)
            } else {
                Toast.makeText(this, "Report name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }



    }
    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherService = retrofit.create(IOpenWeather::class.java)
    }

    private fun requestWeatherData(latitude: Double, longitude: Double, apiKey: String) {
        val weatherDataCall = weatherService.getWeatherData(latitude, longitude, 10, apiKey)
        weatherDataCall.enqueue(object : Callback<WeatherData> {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        // Actualizar datos del adaptador
                        weatherAdapter.updateWeatherData(weatherResponse.list)
                        Toast.makeText(this@ThirdActivity, "Weather Data Retrieved", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(this@ThirdActivity, "Response is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Error fetching weather data: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ThirdActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                Log.e(TAG, "Failure: ${t.message}")
                Toast.makeText(this@ThirdActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }
    private fun addReportToDatabase(report: Map<String, Any>) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("hotspots").push()
        databaseReference.setValue(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Report added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    }



