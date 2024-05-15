package es.upm.btb.helloworldkt

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView
import es.upm.btb.helloworldkt.persistence.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SecondActivity : AppCompatActivity() {
    private val TAG = "SecondActivityRegister"
    private lateinit var database: AppDatabase
    private lateinit var adapter: CoordinatesAdapter
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Toast para la segunda actividad
        Toast.makeText(this, "Has abierto la segunda actividad", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "Register. The second activity has being created.")

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

        // Inflate heading and add to ListView
        listView = findViewById(R.id.lvCoordinates)
        val headerView = layoutInflater.inflate(R.layout.listview_header, listView, false)
        listView.addHeaderView(headerView, null, false)

        // Init adapter
        adapter = CoordinatesAdapter(this, mutableListOf())
        listView.adapter = adapter

        // Init database
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "coordinates").build()
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {
            val itemCount = database.locationDao().getCount()
            Log.d(TAG, "Number of items in database $itemCount.")
            loadCoordinatesFromDatabase(adapter)
        }
    }

    private class CoordinatesAdapter(context: Context, private val coordinatesList: MutableList<List<String>>) :
        ArrayAdapter<List<String>>(context, R.layout.listview_item, coordinatesList) {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(R.layout.listview_item, parent, false)

            val timestampTextView: TextView = view.findViewById(R.id.tvTimestamp)
            val latitudeTextView: TextView = view.findViewById(R.id.tvLatitude)
            val longitudeTextView: TextView = view.findViewById(R.id.tvLongitude)

            try {
                val item = coordinatesList[position]
                timestampTextView.text = formatTimestamp(item[0].toLong())
                latitudeTextView.text = formatCoordinate(item[1].toDouble())
                longitudeTextView.text = formatCoordinate(item[2].toDouble())

                view.setOnClickListener {
                    val intent = Intent(context, ThirdActivity::class.java).apply {
                        putExtra("timestamp", item[0].toLong())
                        putExtra("latitude", item[1].toDouble())
                        putExtra("longitude", item[2].toDouble())
                    }
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CoordinatesAdapter", "getView: Exception parsing coordinates.")
            }
            return view
        }

        private fun formatTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }

        private fun formatCoordinate(value: Double): String {
            return String.format("%.6f", value)
        }

        fun updateData(newData: MutableList<List<String>>) {
            coordinatesList.clear()
            coordinatesList.addAll(newData)
            notifyDataSetChanged()
        }
    }

    private fun loadCoordinatesFromDatabase(adapter: CoordinatesAdapter) {
        lifecycleScope.launch(Dispatchers.IO) {
            val coordinatesList = database.locationDao().getAllLocations()
            val formattedList = coordinatesList.map {
                listOf(it.timestamp.toString(), it.latitude.toString(), it.longitude.toString())
            }
            withContext(Dispatchers.Main) {
                adapter.updateData(formattedList.toMutableList())
            }
            Log.d("CoordinatesAdapter", "Number of items in database ${database.locationDao().getCount()}.")
        }
    }

    fun readFileContents(): List<List<String>> {
        val fileName = "gps_coordinates.csv"
        return try {
            openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.map { it.split(";").map(String::trim) }.toList()
            }
        } catch (e: IOException) {
            listOf(listOf("Error reading file: ${e.message}"))
        }
    }
}
