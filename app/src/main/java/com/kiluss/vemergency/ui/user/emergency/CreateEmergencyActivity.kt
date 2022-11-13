package com.kiluss.vemergency.ui.user.emergency

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.CAR_REPAIR_SERVICE
import com.kiluss.vemergency.constant.EXTRA_PICK_LOCATION
import com.kiluss.vemergency.constant.MOTORCYCLE_REPAIR_SERVICE
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.LatLng
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.ActivityCreateEmergencyBinding
import com.kiluss.vemergency.ui.user.navigation.PickLocationActivity
import com.kiluss.vemergency.utils.Utils

class CreateEmergencyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEmergencyBinding
    private var transaction = Transaction()
    private var location: Location? = null
    private var dialog: AlertDialog? = null
    private val viewModel: CreateEmergencyViewModel by viewModels()
    private val pickLocationFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val location =
                    result.data?.getParcelableExtra<com.google.android.gms.maps.model.LatLng>(EXTRA_PICK_LOCATION)
                transaction.userLocation = LatLng(location?.latitude, location?.longitude)
                binding.tvLocationPicked.text = "${location?.longitude.toString()}, ${location?.latitude.toString()}"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeViewModel()
        getCurrentLocation()
    }

    private fun observeViewModel() {
        with(viewModel) {
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this@CreateEmergencyActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this@CreateEmergencyActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(this@CreateEmergencyActivity).lastLocation
                .addOnSuccessListener(this@CreateEmergencyActivity) { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        this.location = location
                        viewModel.getNearByShop(location, 1, transaction)
                        viewModel.queryNearShop = true
                    }
                    setupView()
                }
        }
    }

    private fun setupView() {
        setupSpinnerCategory()
        binding.llLocation.setOnClickListener {
            val intent = Intent(this, PickLocationActivity::class.java)
            pickLocationFromGalleryForResult.launch(intent)
        }
        onStartSubmit()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setView(R.layout.progress_dialog)
        dialog = builder.create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnDismissListener {
            viewModel.queryNearShop = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun onStartSubmit() {
        with(binding) {
            btnSubmit.setOnClickListener {
                if (binding.spinnerCategory.selectedItem.toString().isNotEmpty()) {
                    transaction.content = edtContent.text.toString()
                    transaction.startTime = Calendar.getInstance().timeInMillis.toDouble()
                    transaction.userId = FirebaseManager.getAuth()?.uid
                    // when user let to use current location
                    if (tvLocationPicked.text.isEmpty()) {
                        if (location != null) {
                            transaction.userLocation = LatLng(location?.latitude, location?.longitude)
                            if (viewModel.shopLists.isEmpty()) {
                            } else {
                                viewModel.sendEmergency(viewModel.shopLists, transaction)
                            }
                            setProgressDialog(true)
                        } else {
                            Utils.showShortToast(
                                this@CreateEmergencyActivity,
                                "Can not get current location, please pick manually"
                            )
                        }
                    } else {
                        val location = Location(LocationManager.GPS_PROVIDER)
                        location.latitude = transaction.userLocation?.latitude!!
                        location.longitude = transaction.userLocation?.longitude!!
                        viewModel.getNearByShop(location, 1, transaction)
                        setProgressDialog(true)
                    }
                } else {
                    Utils.showShortToast(this@CreateEmergencyActivity, "Please choose which service you want to use")
                }
            }
        }
    }

    private fun setProgressDialog(show: Boolean) {
        if (show) dialog?.show() else dialog?.dismiss()
    }

    private fun setupSpinnerCategory() {
        val items = arrayOf(MOTORCYCLE_REPAIR_SERVICE, CAR_REPAIR_SERVICE)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
        binding.spinnerCategory.adapter = adapter
        transaction.service = MOTORCYCLE_REPAIR_SERVICE
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                arg0: AdapterView<*>?,
                arg1: View?,
                arg2: Int,
                arg3: Long
            ) {
                transaction.service = binding.spinnerCategory.selectedItem.toString()
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
