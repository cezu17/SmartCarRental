package com.example.smartcarrental.view

import DirectionsAdapter
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartcarrental.R
import com.example.smartcarrental.network.DirectionsApi
import com.example.smartcarrental.network.Step
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.maps.android.PolyUtil
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RoutePlannerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var tvEtaSummary: TextView
    private lateinit var originInput: TextInputEditText
    private lateinit var destInput: TextInputEditText
    private lateinit var btnGo: Button
    private var lastLegSteps: List<Step> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_planner)

        val sheetCard = findViewById<MaterialCardView>(R.id.controls_card)
        val sheetBehavior = BottomSheetBehavior.from(sheetCard).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            isHideable = false
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        (supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment)
            .getMapAsync(this)

        tvEtaSummary = findViewById(R.id.tvEtaSummary)
        originInput  = findViewById(R.id.origin_input)
        destInput    = findViewById(R.id.destination_input)
        btnGo        = findViewById(R.id.btnGo)

        val apiKey = getString(R.string.google_maps_key)

        val chipDirections = findViewById<Chip>(R.id.chipDirections)
        chipDirections.setOnClickListener {
            if (lastLegSteps.isEmpty()) return@setOnClickListener

            val sheet = BottomSheetDialog(this@RoutePlannerActivity)
            val content = layoutInflater.inflate(R.layout.bottom_sheet_directions, null)
            sheet.setContentView(content)

            val rv =
                content.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvDirections)
            rv.layoutManager = LinearLayoutManager(this@RoutePlannerActivity)
            rv.adapter = DirectionsAdapter(lastLegSteps)

            sheet.show()
        }

        btnGo.setOnClickListener {
            val ori = originInput.text.toString().trim()
            val dst = destInput.text.toString().trim()
            if (ori.isEmpty() || dst.isEmpty()) return@setOnClickListener

            CoroutineScope(Dispatchers.Main).launch {
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                val svc = retrofit.create(DirectionsApi::class.java)

                val resp = svc.getRoute(ori, dst, apiKey)

                map.clear()

                resp.routes.forEachIndexed { idx, route ->
                    val pts = PolyUtil.decode(route.overviewPolyline.points)
                    val color = if (idx == 0) R.color.purple_500 else android.R.color.darker_gray
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(pts)
                            .width(if (idx == 0) 10f else 6f)
                            .color(ContextCompat.getColor(this@RoutePlannerActivity, color))
                    )
                }

                val mainPts = PolyUtil.decode(resp.routes.first().overviewPolyline.points)
                map.addMarker(MarkerOptions().position(mainPts.first()).title("Start"))
                map.addMarker(MarkerOptions().position(mainPts.last()).title("End"))
                val bounds = LatLngBounds.builder()
                    .include(mainPts.first())
                    .include(mainPts.last())
                    .build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

                val leg = resp.routes
                    .firstOrNull()
                    ?.legs
                    ?.firstOrNull()
                if (leg != null) {
                    tvEtaSummary.text = "ETA: ${leg.duration.text} • ${leg.distance.text}"
                    tvEtaSummary.visibility = View.VISIBLE

                    lastLegSteps = leg.steps
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(44.4268, 26.1025), 12f
            ))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
