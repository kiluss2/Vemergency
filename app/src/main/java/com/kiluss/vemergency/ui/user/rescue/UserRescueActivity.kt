package com.kiluss.vemergency.ui.user.rescue

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
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.ACTIVE_SHOP_MARKER
import com.kiluss.vemergency.constant.BOTTOM_SHEET_DIRECTION_STATE
import com.kiluss.vemergency.constant.BOTTOM_SHEET_LIST_SHOP_STATE
import com.kiluss.vemergency.constant.BOTTOM_SHEET_SHOP_PREVIEW_STATE
import com.kiluss.vemergency.constant.EXTRA_NEARBY_LIST
import com.kiluss.vemergency.constant.EXTRA_TRANSACTION
import com.kiluss.vemergency.constant.LATITUDE
import com.kiluss.vemergency.constant.LONGITUDE
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.ActivityUserRescueBinding
import com.kiluss.vemergency.databinding.DialogRescueArrivedBinding
import com.kiluss.vemergency.databinding.DialogReviewBinding
import com.kiluss.vemergency.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.MessageFormat
import java.util.Date

class UserRescueActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityUserRescueBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var map: GoogleMap? = null
    private val viewModel: UserRescueViewModel by viewModels()
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var directing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRescueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        observeViewModel()
        registerForLocationService()
        binding.layoutShopPreview.cvMain.visibility = View.GONE
    }

    private fun registerForLocationService() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @SuppressLint("PotentialBehaviorOverride", "MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        initBottomSheet()
        // adjustMapPaddingToBottomSheet()
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
            val markerTitle: String = it.address.toString()
            val markerOptions =
                MarkerOptions().position(userLocation).title(markerTitle).snippet(it.userFullName).visible(true)
            map?.addMarker(markerOptions)
            markerOptions.anchor(0f, 0.5f)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f))
            setupTransactionInfo()
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
        setupData()
    }

    private fun setupData() {
        intent.getParcelableExtra<Transaction>(EXTRA_TRANSACTION)?.let {
            viewModel.transaction = it
            viewModel.getUpdateShopLocationInfo()
        }
        intent.getParcelableArrayListExtra<Shop>(EXTRA_NEARBY_LIST)?.let {
            showAllShopLocation(it.toMutableList())
        }
    }

    private fun showAllShopLocation(shops: MutableList<Shop>) {
        for (index in shops.indices) {
            val shop = shops[index]
            shop.location?.let {
                val location = LatLng(
                    it.getValue(LATITUDE) as Double, it.getValue(LONGITUDE) as Double
                )
                val markerTitle = shop.name.toString()
                val markerOptions =
                    MarkerOptions().position(location).title(markerTitle).snippet(shop.address).visible(true)
                val marker = map?.addMarker(markerOptions)
                marker?.tag = Pair(ACTIVE_SHOP_MARKER, index)
            }
        }
    }

    private fun setupTransactionInfo() {
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
                            this@UserRescueActivity,
                            Manifest.permission.CALL_PHONE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@UserRescueActivity, arrayOf(Manifest.permission.CALL_PHONE),
                            0
                        )
                    } else {
                        val alertDialog = AlertDialog.Builder(this@UserRescueActivity)
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
            arrived.observe(this@UserRescueActivity) {
                val arrivedDialog =
                    AlertDialog.Builder(this@UserRescueActivity, R.style.DialogRoundedCorner)
                        .create()
                val arrivedDialogBinding =
                    DialogRescueArrivedBinding.inflate(LayoutInflater.from(this@UserRescueActivity))
                arrivedDialogBinding.btnOk.setOnClickListener {
                    arrivedDialog.dismiss()
                }
                with(arrivedDialog) {
                    setView(arrivedDialogBinding.root)
                    show()
                }
                with(binding) {
                    tvBottomSheetTitle.visibility = View.INVISIBLE
                    btnFinish.visibility = View.VISIBLE
                    btnFinish.setOnClickListener {
                        val reviewDialog =
                            AlertDialog.Builder(this@UserRescueActivity, R.style.DialogRoundedCorner)
                                .create()
                        val reviewDialogBinding =
                            DialogReviewBinding.inflate(LayoutInflater.from(this@UserRescueActivity))
                        reviewDialogBinding.btnOk.setOnClickListener {
                            reviewDialog.dismiss()
                            viewModel.finishTransaction(
                                reviewDialogBinding.rbRate.rating.toDouble(),
                                reviewDialogBinding.edtReview.text.toString()
                            )
                        }
                        with(reviewDialog) {
                            setView(reviewDialogBinding.root)
                            show()
                        }
                    }
                }
            }
            update.observe(this@UserRescueActivity) {
                if (viewModel.shop == null) {
                    viewModel.getShopRescueInfo()
                }
                with(binding) {
                    tvDistance.text = "${getString(R.string.distance)} ${viewModel.transaction.distance} km"
                    tvEstimateTime.text =
                        "${getString(R.string.estimate_time)} ${
                            viewModel.transaction.duration?.toInt()
                                ?.let { it1 -> Utils.convertSeconds(it1) }
                        }"
                    lnDirection.visibility = View.VISIBLE
                }
            }
            shopValue.observe(this@UserRescueActivity) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                with(binding.layoutShopPreview) {
                    tvShopTitle.text = shop?.name
                    tvShopAddress.text = shop?.address
                    tvService.text = shop?.service
                    val shopRating = shop?.rating
                    if (shopRating != null) {
                        rbRating.visibility = View.VISIBLE
                        tvNoRating.visibility = View.GONE
                        rbRating.rating = shopRating.toFloat()
                    } else {
                        rbRating.visibility = View.GONE
                        tvNoRating.visibility = View.VISIBLE
                    }
                    shop?.imageUrl?.let {
                        Glide.with(this@UserRescueActivity).load(shop?.imageUrl).placeholder(R.drawable.default_pic)
                            .centerCrop()
                            .into(ivShopImage)
                    }
                    cvMain.visibility = View.VISIBLE
                }
            }
            finishActivity.observe(this@UserRescueActivity) {
                finish()
            }
        }
    }

    private fun setBottomSheetShowingState(state: String) {
        with(binding) {
            when (state) {
                BOTTOM_SHEET_DIRECTION_STATE -> {
                    bottomSheetBehavior.halfExpandedRatio = 0.4f
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

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        } else {
            super.onBackPressed()
        }
    }
}