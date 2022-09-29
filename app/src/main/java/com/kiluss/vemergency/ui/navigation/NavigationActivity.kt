package com.kiluss.vemergency.ui.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kiluss.vemergency.R
import com.kiluss.vemergency.databinding.ActivityNavigationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityNavigationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val daNang = LatLng(16.0545, 108.0717)
        mMap.addMarker(
            MarkerOptions()
                .position(daNang)
                .title("Marker in Da Nang")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(daNang))
    }
}
