package com.kiluss.vemergency.ui.user.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import com.kiluss.vemergency.BuildConfig.MAPS_API_KEY
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ActivityNavigationBinding
import com.kiluss.vemergency.ui.admin.approve.ApproveShopActivity
import com.kiluss.vemergency.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.MessageFormat

class NavigationActivity : AppCompatActivity(), OnMapReadyCallback, ShopPreviewAdapter.OnClickListener {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var map: GoogleMap? = null
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var myShop: Shop
    private val viewModel: NavigationViewModel by viewModels()
    private var currentLocation: Location? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var shopCloneAdapter: ShopPreviewAdapter? = null
    private var currentPolylines: Polyline? = null
    private var directing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLocationPermission()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        setUpRecyclerViewListView()
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
        //viewModel.getAllShopLocation()
        //viewModel.getAllCloneShopLocation()
        // Add a marker and move the camera
        var location = LatLng(0.0, 0.0)
        var markerTitle = "Marker in Da Nang"
        var zoom = 6f

        if (intent.getStringExtra(EXTRA_LAUNCH_MAP) != null) {
            location = LatLng(16.0, 108.2)
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener(this) { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        viewModel.getNearByShop(location, 1)
                    }
                }
            }
        } else if (intent.getParcelableExtra<LatLng>(EXTRA_SHOP_LOCATION) != null) {
            myShop = intent.getParcelableExtra(EXTRA_SHOP_LOCATION)!!
            location = LatLng(
                myShop.location?.getValue(LATITUDE)!! as Double, myShop.location?.getValue(LONGITUDE)!! as Double
            )
            markerTitle = myShop.name.toString()
            zoom = 16f
            val markerOptions = MarkerOptions().position(location).title(markerTitle).snippet(markerTitle).visible(true)
            map?.addMarker(markerOptions)
            markerOptions.anchor(0f, 0.5f)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            location = LatLng(16.0, 108.2)
        }
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
        map?.setOnMapClickListener {
            if (!directing) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.tvBottomSheetTitle.text = MessageFormat.format(
                    resources.getText(R.string.text_found_near_by).toString(), viewModel.getShopClone().size
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
            if (!directing) {
                marker.tag?.let { tag ->
                    val shop = viewModel.getShopCloneInfo(tag as Int)
                    setShopPreviewInfo(shop)
                }
            }
            true
        }
        binding.ivBack.setOnClickListener {
            directing = false
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.tvBottomSheetTitle.text = MessageFormat.format(
                resources.getText(R.string.text_found_near_by).toString(), viewModel.getShopClone().size
            )
            setBottomSheetShowingState(BOTTOM_SHEET_LIST_SHOP_STATE)
        }
//        mMap.setOnMapClickListener { position ->
//            Toast.makeText(
//                this,
//                "Lat " + position.latitude + " "
//                        + "Long " + position.longitude,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }

    private fun setShopPreviewInfo(shop: Shop) {
        binding.tvBottomSheetTitle.text = shop.name
        with(binding.layoutShopPreview) {
            tvShopTitle.text = shop.name
            tvShopAddress.text = shop.address
            tvService.text = shop.service
            val shopRating = shop.rating
            if (shopRating != null) {
                rbRating.visibility = View.VISIBLE
                tvNoRating.visibility = View.GONE
                rbRating.rating = shopRating.toFloat()
            } else {
                rbRating.visibility = View.GONE
                tvNoRating.visibility = View.VISIBLE
            }
            shop.imageUrl?.let {
                Glide.with(this@NavigationActivity).load(shop.imageUrl).placeholder(R.drawable.default_pic).centerCrop()
                    .into(ivShopImage)
            }
        }
        binding.ivDirection.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener(this@NavigationActivity) { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        directing = true
                        setBottomSheetShowingState(BOTTOM_SHEET_DIRECTION_STATE)
                        currentPolylines?.remove()
                        binding.tvBottomSheetTitle.text = getString(R.string.direction_from_your_location)
                        binding.tvDirectionTo.text = shop.name
                        direction(
                            LatLng(location.latitude, location.longitude), LatLng(
                                shop.location?.get(LATITUDE) as Double, shop.location?.get(LONGITUDE) as Double
                            )
                        )
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            allShop.observe(this@NavigationActivity) {
                showAllShopLocation(it)
                setBottomSheetShowingState(BOTTOM_SHEET_LIST_SHOP_STATE)
            }
            cloneShop.observe(this@NavigationActivity) {
                showAllCloneShopLocation(it)
                shopCloneAdapter?.updateData(it)
                binding.tvBottomSheetTitle.text = MessageFormat.format(
                    resources.getText(R.string.text_found_near_by).toString(), it.size
                )
                setBottomSheetShowingState(BOTTOM_SHEET_LIST_SHOP_STATE)
            }
        }
    }

    private fun setBottomSheetShowingState(state: String) {
        with(binding) {
            when (state) {
                BOTTOM_SHEET_LIST_SHOP_STATE -> {
                    if (!directing) {
                        bottomSheetBehavior.halfExpandedRatio = 0.5f
                        layoutShopPreview.cvMain.visibility = View.GONE
                        rvShopList.visibility = View.VISIBLE
                        ivDirection.visibility = View.GONE
                        lnDirection.visibility = View.GONE
                        ivBack.visibility = View.GONE
                    }
                }
                BOTTOM_SHEET_SHOP_PREVIEW_STATE -> {
                    if (!directing) {
                        bottomSheetBehavior.halfExpandedRatio = 0.42f
                        layoutShopPreview.cvMain.visibility = View.VISIBLE
                        rvShopList.visibility = View.GONE
                        ivDirection.visibility = View.VISIBLE
                        lnDirection.visibility = View.GONE
                        ivBack.visibility = View.GONE
                    }
                }
                BOTTOM_SHEET_DIRECTION_STATE -> {
                    bottomSheetBehavior.halfExpandedRatio = 0.4f
                    layoutShopPreview.cvMain.visibility = View.GONE
                    rvShopList.visibility = View.GONE
                    ivDirection.visibility = View.GONE
                    lnDirection.visibility = View.VISIBLE
                    ivBack.visibility = View.VISIBLE
                }
                else -> {
                    tvBottomSheetTitle.text = resources.getText(R.string.app_name)
                    layoutShopPreview.cvMain.visibility = View.GONE
                    rvShopList.visibility = View.GONE
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

    private fun setUpRecyclerViewListView() {
        shopCloneAdapter = ShopPreviewAdapter(mutableListOf(), this, this)
        with(binding.rvShopList) {
            adapter = shopCloneAdapter
            layoutManager = LinearLayoutManager(this@NavigationActivity, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun showAllShopLocation(shops: MutableList<Shop>) {
        shops.forEach { shop ->
            shop.location?.let {
                val location = LatLng(
                    it.getValue(LATITUDE) as Double, it.getValue(LONGITUDE) as Double
                )
                val markerTitle = shop.name.toString()
                val markerOptions =
                    MarkerOptions().position(location).title(markerTitle).snippet(shop.address).visible(true)
                map?.addMarker(markerOptions)
            }
        }
    }

    private fun showAllCloneShopLocation(shops: MutableList<Shop>) {
        for (index in shops.indices) {
            val shop = shops[index]
            shop.location?.let {
                val location = LatLng(
                    it.getValue(LATITUDE) as Double, it.getValue(LONGITUDE) as Double
                )
                val markerTitle = shop.name.toString()
                val markerOptions =
                    MarkerOptions().position(location).title(markerTitle).snippet(shop.address).visible(true).icon(
                        BitmapDescriptorFactory.defaultMarker(25F)
                    )
                val marker = map?.addMarker(markerOptions)
                marker?.tag = index
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this).setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }.create().show()
            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
            //checkBackgroundLocation()
        }
    }

    private fun checkBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ), MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ), MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun directionApi() {
        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections =
            "http://router.project-osrm.org/route/v1/driving/16.057258%2C108.215804&loc=16.033316%2C108.224602&loc=16.081816%2C108.146324?overview=false"
        "https://maps.googleapis.com/maps/api/directions/json?origin=16.073503,108.161034&destination=16.072937,108.213773&key=$MAPS_API_KEY"
        val directionsRequest = object : StringRequest(Method.GET, urlDirections, Response.Listener { response ->
            try {
                val jsonResponse = JSONObject(response)
                println(response.toString())
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    map?.addPolyline(
                        PolylineOptions().addAll(path[i]).color(Color.BLUE).jointType(JointType.ROUND)
                            .width(POLYLINE_STROKE_WIDTH_PX)
                    )
                }
            } catch (exception: JSONException) {
                exception.printStackTrace()
            }
        }, Response.ErrorListener {}) {}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
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
                currentPolylines = map?.addPolyline(option)
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder().apply {
                    include(departure)
                    include(destination)
                }.build(), 100))
                with(binding) {
                    val df = DecimalFormat("#.#")
                    df.roundingMode = RoundingMode.CEILING
                    tvDistance.text = "${getString(R.string.distance)} ${df.format(road.mLength)} km"
                    tvEstimateTime.text = "${getString(R.string.estimate_time)} ${Utils.convertSeconds(road.mDuration.toInt())}"
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val mMap = map
                        mMap?.isMyLocationEnabled = true
                        // Now check background location
                        checkBackgroundLocation()
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null),
                            ),
                        )
                    }
                }
                return
            }
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            this, "Granted Background Location Permission", Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onOpen(shop: Shop) {
        startActivity(Intent(this, ApproveShopActivity::class.java).apply {
            putExtra(EXTRA_SHOP_DETAIL, "")
            putExtra(EXTRA_SHOP_PENDING, shop)
        })
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        } else {
            super.onBackPressed()
        }
    }
}
