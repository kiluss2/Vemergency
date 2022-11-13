package com.kiluss.vemergency.ui.shop.rescue

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.BOTTOM_SHEET_DIRECTION_STATE
import com.kiluss.vemergency.constant.BOTTOM_SHEET_LIST_SHOP_STATE
import com.kiluss.vemergency.constant.BOTTOM_SHEET_SHOP_PREVIEW_STATE
import com.kiluss.vemergency.constant.EXTRA_TRANSACTION
import com.kiluss.vemergency.constant.POLYLINE_STROKE_WIDTH_PX
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.ActivityShopRescueBinding
import com.kiluss.vemergency.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.MessageFormat
import java.util.Date

class ShopRescueActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityShopRescueBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var map: GoogleMap? = null
    private lateinit var myShop: Shop
    private val viewModel: ShopRescueViewModel by viewModels()
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentPolyLines: Polyline? = null
    private var directing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopRescueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        observeViewModel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        initBottomSheet()
        //adjustMapPaddingToBottomSheet()
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style_json
                )
            )
            if (!success) {
                Log.e("TAG", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("TAG", "Can't find style. Error: ", e)
        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val mMap = map
            mMap?.isMyLocationEnabled = true
        }
        setupMapView()
    }

    @SuppressLint("PotentialBehaviorOverride", "MissingPermission")
    private fun setupMapView() {
        intent.getParcelableExtra<Transaction>(EXTRA_TRANSACTION)?.let {
            val userLocation = LatLng(it.userLocation?.latitude!!, it.userLocation?.longitude!!)
            viewModel.transaction = it
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener(this) { lastLocation ->
                    // Got last known location. In some rare situations this can be null.
                    if (lastLocation != null) {
                        // Logic to handle location object
                        viewModel.currentLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        direction(viewModel.currentLocation, userLocation)
                    }
                }
            }
            val markerTitle: String = it.address.toString()
            val markerOptions =
                MarkerOptions().position(userLocation).title(markerTitle).snippet(it.userFullName).visible(true)
            map?.addMarker(markerOptions)
            markerOptions.anchor(0f, 0.5f)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f))
            setupUserInfo()
        }
        map?.setOnMapClickListener {
            if (!directing) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.tvBottomSheetTitle.text = MessageFormat.format(
                    resources.getText(R.string.text_found_near_by).toString(), viewModel.getNearByShopNumber()
                )
                setBottomSheetShowingState(BOTTOM_SHEET_LIST_SHOP_STATE)
            }
        }
        map?.setOnMarkerClickListener { marker ->
            setBottomSheetShowingState(BOTTOM_SHEET_SHOP_PREVIEW_STATE)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            lifecycleScope.launch(Dispatchers.IO) {
                delay(50)
                launch(Dispatchers.Main) {
                    if (marker.isInfoWindowShown) {
                        marker.hideInfoWindow()
                    } else {
                        marker.showInfoWindow()
                    }
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 16f))
                }
            }
            true
        }
        binding.ivBack.setOnClickListener {
            directing = false
            currentPolyLines?.remove()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.tvBottomSheetTitle.text = MessageFormat.format(
                resources.getText(R.string.text_found_near_by).toString(), viewModel.getNearByShopNumber()
            )
            setBottomSheetShowingState(BOTTOM_SHEET_LIST_SHOP_STATE)
        }
    }

    private fun setupUserInfo() {
        with(viewModel.transaction) {
            with(binding.layoutUserInfo) {
                tvAddress.text = address
                tvPhone.text = userPhone
                tvService.text = service
                tvContent.text = content
                tvFullName.text = userFullName
                tvTime.text = startTime?.toLong()?.let { Date(it).toString() }
                btnSelect.visibility = View.GONE
                tvPhone.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(
                            this@ShopRescueActivity,
                            android.Manifest.permission.CALL_PHONE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@ShopRescueActivity, arrayOf(android.Manifest.permission.CALL_PHONE),
                            0
                        )
                    } else {
                        val alertDialog = AlertDialog.Builder(this@ShopRescueActivity)
                        alertDialog.apply {
                            setIcon(R.drawable.ic_call)
                            setTitle("Make a phone call?")
                            setMessage("Do you want to make a phone call?")
                            setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                                // make phone call
                                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$userPhone"))
                                context.startActivity(intent)
                            }
                            setNegativeButton("No") { _, _ ->
                            }
                        }.create().show()
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
        }
    }

    private fun setBottomSheetShowingState(state: String) {
        with(binding) {
            when (state) {
                BOTTOM_SHEET_DIRECTION_STATE -> {
                    bottomSheetBehavior.halfExpandedRatio = 0.4f
                    ivDirection.visibility = View.GONE
                    lnDirection.visibility = View.VISIBLE
                    ivBack.visibility = View.VISIBLE
                }
                else -> {
                    tvBottomSheetTitle.text = resources.getText(R.string.app_name)
                }
            }
        }
    }

    private fun initBottomSheet() {
        setBottomSheetShowingState("")
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.clBottomSheet))
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.halfExpandedRatio = 0.6f
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                when (bottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                        adjustMapPaddingToBottomSheet()
                    }
                    else -> {
                        // No adjustment needed on other slide states.
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Needed only in case you manually change the bottomsheet's state in code somewhere.
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Nothing to do here
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        // Nothing to do here
                    }
                    else -> {
                        adjustMapPaddingToBottomSheet()
                    }
                }
            }
        })
        binding.tvBottomSheetTitle.setOnClickListener {
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
                else -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
    }

    private fun adjustMapPaddingToBottomSheet() {
        map?.setPadding(
            0, 0, 0, binding.rootLayout.height - binding.clBottomSheet.top
        )
    }

    private fun direction(departure: LatLng, destination: LatLng) {
        val paths = mutableListOf<List<LatLng>>()
        val roadManager = OSRMRoadManager(this, "MY_USER_AGENT")
        val waypoints = arrayListOf<GeoPoint>()
        waypoints.add(GeoPoint(departure.latitude, departure.longitude))
        waypoints.add(GeoPoint(destination.latitude, destination.longitude))
        lifecycleScope.launch(Dispatchers.IO) {
            val road = roadManager.getRoad(waypoints)
            launch(Dispatchers.Main) {
                val steps = RoadManager.buildRoadOverlay(road).actualPoints
                for (i in 0 until steps.size - 1) {
                    val path = listOf(
                        LatLng(steps[i].latitude, steps[i].longitude),
                        LatLng(steps[i + 1].latitude, steps[i + 1].longitude)
                    )
                    paths.add(path)
                }
                val option = PolylineOptions().color(getColor(R.color.blue_shazam)).width(POLYLINE_STROKE_WIDTH_PX)
                for (i in 0 until paths.size) {
                    option.addAll(paths[i])
                }
                currentPolyLines = map?.addPolyline(option)
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder().apply {
                    include(departure)
                    include(destination)
                }.build(), 300))
                with(binding) {
                    val df = DecimalFormat("#.#")
                    df.roundingMode = RoundingMode.CEILING
                    tvDistance.text = "${getString(R.string.distance)} ${df.format(road.mLength)} km"
                    tvEstimateTime.text =
                        "${getString(R.string.estimate_time)} ${Utils.convertSeconds(road.mDuration.toInt())}"
                }
            }
        }
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        } else {
            super.onBackPressed()
        }
    }
}
