package es.upm.btb.helloworldkt

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.room.Room
import es.upm.btb.helloworldkt.persistence.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThirdActivity : AppCompatActivity() {
    private val TAG = "ThirdActivityRegister"

    lateinit var database: AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        // Toast adicional para la tercera actividad
        Toast.makeText(this, "Has abierto la tercera actividad", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "Register. The third activity has being created.");


    // Get coordinates for selected item. Set default ones if not obtained.
        val timestamp = intent.getLongExtra("timestamp", 0)
        val latitud = intent.getDoubleExtra("latitude", 40.475172)
        val longitud = intent.getDoubleExtra("longitude", -3.461757)

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
                }
                .setNegativeButton("No", null)
                .show()
        }



    }

    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }

    }



