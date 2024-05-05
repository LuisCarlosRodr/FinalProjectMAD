package es.upm.btb.helloworldkt

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class OpenStreetMapActivity : AppCompatActivity() {
    private val TAG = "RegisterOpenStreetMapActivity"
    private lateinit var map: MapView

    val gymkhanaCoords = listOf(
        GeoPoint(40.38779608214728, -3.627687914352839), // Tennis
        GeoPoint(40.38788595319803, -3.627048250272035), // Futsal outdoors
        GeoPoint(40.3887315224542, -3.628643539758645), // Fashion and design
        GeoPoint(40.38926842612264, -3.630067893975619), // Topos
        GeoPoint(40.38956358584258, -3.629046081389352), // Teleco
        GeoPoint(40.38992125672989, -3.6281366497769714), // ETSISI
        GeoPoint(40.39037466191718, -3.6270256763598447), // Library
        GeoPoint(40.389855884803005, -3.626782180787362) // CITSEM
    )

    val gymkhanaNames = listOf(
        "Tennis",
        "Futsal outdoors",
        "Fashion and design school",
        "Topography school",
        "Telecommunications school",
        "ETSISI",
        "Library",
        "CITSEM"
    )

    val foodSitesOnMadrid = listOf(
        GeoPoint(40.42064, -3.70621), // LaBorraDelCafé
        GeoPoint(40.42054, -3.70605), // PaPizza
        GeoPoint(40.42088, -3.70557), // Kiraku
        GeoPoint(40.42144, -3.7061), // Kokoxaxa
        GeoPoint(40.42003, -3.70547), // StarBucks
        GeoPoint(40.41948, -3.70581), // Rodilla
        GeoPoint(40.42013, -3.70771), // La Libanesa
        GeoPoint(40.42037, -3.70809) // Cafeteria Oskar
    )

    val foodSitesonMadridNames = listOf(
        "LaBorraDelCafé",
        "PaPizza",
        "Kiraku",
        "Kokoxaxa",
        "Starbucks",
        "Rodilla",
        "La Libanesa",
        "Cafeteria Oskar"
    )

    // The second route helps you to get a mini-tour in Pradillo. Pradillo is the main place located on Mostoles.
    val MostolesSites = listOf(
        GeoPoint(40.32170, -3.86483), // Pradillo
        GeoPoint(40.32137, -3.86508), // CeX
        GeoPoint(40.32286, -3.86368), // Cajamar
        GeoPoint(40.32310, -3.86409), // CaiXaBank
        GeoPoint(40.32258, -3.86494), // La Tasquita
        GeoPoint(40.32270, -3.86549), // Ayuntamiento de Mostoles
        GeoPoint(40.32222, -3.86486), // BBVA
        GeoPoint(40.32146, -3.86528) // Churreria Pradillo
    )

    val sitesOnMostoles = listOf(
        "Pradillo",
        "CeX",
        "Cajamar",
        "CaiXaBank",
        "La Tasquita",
        "Ayuntamiento de Mostoles",
        "BBVA",
        "Churreria Pradillo"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_street_map)

        Log.d(TAG, "onCreate: The activity is being created.");

        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable("location")

        if (location != null) {
            Log.i(TAG, "onCreate: Location["+location.altitude+"]["+location.latitude+"]["+location.longitude+"][")

            Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))

            map = findViewById(R.id.map)
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.controller.setZoom(18.0)

            val startPoint = GeoPoint(location.latitude, location.longitude)
            map.controller.setCenter(startPoint)

            addMarker(startPoint, "Mi actual localización")
            //addMarkers(map, gymkhanaCoords, gymkhanaNames)
            addMarkersAndRoute(map, gymkhanaCoords, gymkhanaNames)
            addMarkersAndRoute(map,foodSitesOnMadrid,foodSitesonMadridNames)
            addMarkersAndRoute(map,MostolesSites,sitesOnMostoles)
        };
    }

    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        map.overlays.add(marker)
        map.invalidate() // Reload map
    }

    fun addMarkers(mapView: MapView, locationsCoords: List<GeoPoint>, locationsNames: List<String>) {

        for (location in locationsCoords) {
            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Marker at ${locationsNames.get(locationsCoords.indexOf(location))} ${location.latitude}, ${location.longitude}"
            marker.icon = ContextCompat.getDrawable(this, com.google.android.material.R.drawable.ic_m3_chip_close)
            mapView.overlays.add(marker)
        }
        mapView.invalidate() // Refresh the map to display the new markers
    }

    fun addMarkersAndRoute(mapView: MapView, locationsCoords: List<GeoPoint>, locationsNames: List<String>) {
        if (locationsCoords.size != locationsNames.size) {
            Log.e("addMarkersAndRoute", "Locations and names lists must have the same number of items.")
            return
        }

        val route = Polyline()
        route.setPoints(locationsCoords)
        route.color = ContextCompat.getColor(this, R.color.purple_700)
        mapView.overlays.add(route)

        for (location in locationsCoords) {
            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            val locationIndex = locationsCoords.indexOf(location)
            marker.title = "Marker at ${locationsNames[locationIndex]} ${location.latitude}, ${location.longitude}"
            // For the icons we decided to use the ones from the library
            marker.icon = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.center)
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }



    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}