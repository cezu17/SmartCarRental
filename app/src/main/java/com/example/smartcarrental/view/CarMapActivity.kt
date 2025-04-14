package com.example.smartcarrental.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcarrental.R
import com.example.smartcarrental.viewmodel.CarViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val carViewModel: CarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_return)
            .setOnClickListener {
                finish()
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        // Center the map on Bucharest
        val bucharestCenter = LatLng(44.4268, 26.1025)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bucharestCenter, 12f))

        val locationCSIE = LatLng(44.4479606,26.0965661)

        carViewModel.allCars.observe(this) { cars ->
            cars.forEach { car ->
                var carPosition = LatLng(44.40 + Math.random() * 0.1, 26.00 + Math.random() * 0.2)

                if (car.make.equals("Ford", ignoreCase = true) && car.model.equals("Focus", ignoreCase = true)) {
                    carPosition = locationCSIE
                }

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(carPosition)
                        .title("${car.make} ${car.model}")
                        .snippet("${car.price}€/day")
                )
                marker?.tag = car.id
            }
        }

        map.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        map.setOnInfoWindowClickListener { marker ->
            val carId = marker.tag as? Long
            carId?.let {
                startActivity(BookingActivity.newIntent(this, it))
            }
        }
    }
}
