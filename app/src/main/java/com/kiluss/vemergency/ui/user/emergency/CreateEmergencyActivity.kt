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
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.LatLng
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.Transaction
import com.kiluss.vemergency.databinding.ActivityCreateEmergencyBinding
import com.kiluss.vemergency.network.api.ApiService
import com.kiluss.vemergency.network.api.RetrofitClient
import com.kiluss.vemergency.ui.user.navigation.PickLocationActivity
import com.kiluss.vemergency.utils.Utils
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateEmergencyActivity : AppCompatActivity() {

    private var isStarting = false
    private lateinit var binding: ActivityCreateEmergencyBinding
    private var transaction = Transaction()
    private var location: Location? = null
    private val db = Firebase.firestore
    private var dialog: AlertDialog? = null
    private val shopLists = mutableListOf<Shop>()
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
                        getNearByShop(location, 1)
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
    }

    @SuppressLint("MissingPermission")
    private fun onStartSubmit() {
        with(binding) {
            btnSubmit.setOnClickListener {
                if (binding.spinnerCategory.selectedItem.toString().isNotEmpty()) {
                    transaction.content = edtContent.text.toString()
                    transaction.startTime = Calendar.getInstance().timeInMillis.toDouble()
                    transaction.userUid = FirebaseManager.getAuth()?.uid
                    if (tvLocationPicked.text.isEmpty()) {
                        if (location != null) {
                            transaction.userLocation = LatLng(location?.latitude, location?.longitude)
                            if (shopLists.isEmpty()) {
                                isStarting = true
                            } else {
                                sendEmergency()
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
                        isStarting = true
                        getNearByShop(location, 1)
                        setProgressDialog(true)
                    }
                } else {
                    Utils.showShortToast(this@CreateEmergencyActivity, "Please choose which service you want to use")
                }
            }
        }
    }

    private fun sendEmergency() {
        println(shopLists)
        val tokens = JsonArray()
        tokens.add("ftZKtB2wRAuLTDaS7tg4ov:APA91bHbDmyyoV8Rd8rVexKBdSC2EbrzNkVxQVyx1efXfQFsP-7xAlHfx6Nfk9_IwnxRIM1Vteu60PfTdjkvISyTzzgze6KjjeWvE4VQmEuUaJNn-ONhkU-oboeoLhD68amyJwxbmDHx")
        tokens.add("fEOFrZcPRLuiVDjbhsVHdg:APA91bFD9hXYgJqawGXEFbM8Cds75wFaY17hODj5lkhOiTfnoelJlwj4OoKX3k5_5Zlkos3EKXs1aLNG0RZYWnTi4jhLZKEw0bA3DrJELyZFs6AH0fb83uTnvMfloRGAiTNXH_ItqEbO")
        RetrofitClient.getInstance(this).getClientUnAuthorize(SEND_NOTI_API_URL).create(ApiService::class.java)
            .sendNoti(tokens.toString().toRequestBody())
            .enqueue(object : Callback<JsonObject?> {
                override fun onResponse(
                    call: Call<JsonObject?>,
                    response: Response<JsonObject?>
                ) {
                    Log.e("emergency", response.body().toString())
                    when {
                        response.isSuccessful -> {
                            Log.e("emergency", response.body().toString())
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Log.e("emergency", t.toString())
                    t.printStackTrace()
                }
            })
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

    // get active shop near by
    private fun getNearByShop(location: Location, radiusKmRange: Int) {
        val center = GeoLocation(location.latitude, location.longitude)
        // query $radiusKmRange km around the location
        val radiusInM = (radiusKmRange * 1000).toDouble()
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = db.collection(SHOP_COLLECTION)
                .orderBy("location\$app_debug.$GEO_HASH")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(q.get())
        }
        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
                for (task in tasks) {
                    val snap: QuerySnapshot = task.result
                    for (doc in snap.documents) {
                        val shop = doc.toObject<Shop>()
                        val lat = shop?.location?.getValue(LATITUDE)
                        val lng = shop?.location?.getValue(LONGITUDE)
                        // We have to filter out a few false positives due to GeoHash
                        // accuracy, but most will match
                        if (lat != null && lng != null) {
                            val docLocation = GeoLocation(lat as Double, lng as Double)
                            val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                            if (distanceInM <= radiusInM) {
                                matchingDocs.add(doc)
                            }
                        }
                    }
                }
                if (matchingDocs.isEmpty()) {
                    getNearByShop(location, radiusKmRange + radiusKmRange % 10 + 1)
                    println(radiusKmRange + radiusKmRange % 10 + 1)
                } else {
                    // matchingDocs contains the results
                    val list = mutableListOf<Shop>()
                    for (documentSnapshot in matchingDocs) {
                        val item = documentSnapshot.toObject<Shop>()
                        item?.let { it1 -> list.add(it1) }
                    }
                    shopLists.addAll(list)
                    if (isStarting) {
                        sendEmergency()
                    }
                }
            }
    }

    override fun onBackPressed() {
        if (isStarting) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
