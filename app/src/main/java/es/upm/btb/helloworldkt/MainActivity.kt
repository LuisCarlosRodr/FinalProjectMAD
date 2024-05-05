package es.upm.btb.helloworldkt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "MainActivityRegister"
    private lateinit var locationManager: LocationManager
    var latestLocation: Location?= null
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonOsm: Button = findViewById(R.id.osmButton)

        buttonOsm.setOnClickListener {
            if (latestLocation != null) {
                val intent = Intent(this, OpenStreetMapActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable("location", latestLocation)
                intent.putExtra("locationBundle", bundle)
                startActivity(intent)
            }else{
                Log.e(TAG, "Location not set yet.")
            }
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionCode
            )
        } else {
            // The location is updated every 5000 milliseconds (or 5 seconds) and/or if the device moves more than 5 meters,
            // whichever happens first
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            Log.i(TAG, "Location updates requested successfully.")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
                    Log.i(TAG, "Location updates requested successfully after permission granted.")
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        latestLocation = location
        val textView: TextView = findViewById(R.id.mainTextView)
        textView.text = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        Log.d(TAG, "onLocationChanged: Latitude: ${location.latitude}, Longitude: ${location.longitude}")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.w(TAG, "onStatusChanged: Provider: $provider, Status: $status")
    }

    override fun onProviderEnabled(provider: String) {
        Log.i(TAG, "onProviderEnabled: Provider $provider enabled.")
    }

    override fun onProviderDisabled(provider: String) {
        Log.w(TAG, "onProviderDisabled: Provider $provider disabled.")
    }
}
