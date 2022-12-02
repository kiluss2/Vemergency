package com.kiluss.vemergency.ui.shop.addshop

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.CAR_MOTORCYCLE_REPAIR_SERVICE
import com.kiluss.vemergency.constant.CAR_REPAIR_SERVICE
import com.kiluss.vemergency.constant.EXTRA_CREATED_SHOP
import com.kiluss.vemergency.constant.EXTRA_PICK_LOCATION
import com.kiluss.vemergency.constant.GEO_HASH
import com.kiluss.vemergency.constant.IMAGE_API_URL
import com.kiluss.vemergency.constant.LATITUDE
import com.kiluss.vemergency.constant.LONGITUDE
import com.kiluss.vemergency.constant.MAX_WIDTH_IMAGE
import com.kiluss.vemergency.constant.MOTORCYCLE_REPAIR_SERVICE
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.SHOP_PENDING_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.LatLng
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ActivityAddNewShopBinding
import com.kiluss.vemergency.network.api.ApiService
import com.kiluss.vemergency.network.api.RetrofitClient
import com.kiluss.vemergency.ui.shop.main.ShopMainActivity
import com.kiluss.vemergency.ui.user.navigation.PickLocationActivity
import com.kiluss.vemergency.utils.URIPathHelper
import com.kiluss.vemergency.utils.Utils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddNewShopActivity : AppCompatActivity() {
    private var imageUrl: String? = null
    private lateinit var binding: ActivityAddNewShopBinding
    private var imageBase64: String? = null
    private var shop = Shop()
    private var location: LatLng? = null
    private val db = Firebase.firestore
    private lateinit var imageApi: ApiService
    private val requestManageStoragePermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val pickImageFromGalleryForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val imagePath = intent?.data?.let { URIPathHelper().getPath(this, it) } ?: ""
            // handle image from gallery
            val file = File(imagePath)
            val imageBitmap = Utils.getResizedBitmap(file, MAX_WIDTH_IMAGE)
            binding.ivCoverPicked.setImageBitmap(imageBitmap)
            imageBase64 = Utils.encodeImageToBase64String(imageBitmap)
        }
    }
    private val pickLocationFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val location =
                    result.data?.getParcelableExtra<com.google.android.gms.maps.model.LatLng>(EXTRA_PICK_LOCATION)
                this.location = LatLng(location?.latitude, location?.longitude)
                binding.tvLocationPicked.text = "${location?.longitude.toString()}, ${location?.latitude.toString()}"
            }
        }
    private val requestReadPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    this, getString(R.string.permission_denied), Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        imageApi = RetrofitClient.getInstance(this).getClientUnAuthorize(IMAGE_API_URL).create(ApiService::class.java)
    }

    private fun setupView() {
        with(binding) {
            llCover.setOnClickListener {
                pickImage()
            }
            llLocation.setOnClickListener {
                val intent = Intent(this@AddNewShopActivity, PickLocationActivity::class.java)
                pickLocationFromGalleryForResult.launch(intent)
            }
            btnSubmit.setOnClickListener {
                db.collection("shops").whereEqualTo("phone", edtPhone.text.toString())
                    .get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (!task.result.isEmpty) {
                                edtPhone.error = "Phone number is already taken"
                            } else {
                                upLoadInfo()
                            }
                        } else {
                            Log.d("add new shop", "Error getting documents: ", task.exception)
                        }
                    }
            }
            setupSpinnerCategory()
        }
    }

    private fun upLoadInfo() {
        with(binding) {
            if (edtName.text.isEmpty()) {
                edtName.error = "Name can not empty"
            }
            if (edtAddress.text.isEmpty()) {
                edtAddress.error = "Address can not empty"
            }
            if (edtPhone.text.isEmpty()) {
                edtPhone.error = "Phone can not empty"
            }
            if (tvLocationPicked.text.isEmpty()) {
                Utils.showShortToast(this@AddNewShopActivity, "Please choose location")
            }
            if (edtName.text.isNotEmpty() && edtAddress.text.isNotEmpty() && edtPhone.text.isNotEmpty() && tvLocationPicked.text.isNotEmpty()) {
                showProgressbar()
                uploadShopImage()
            }
        }
    }

    private fun setupSpinnerCategory() {
        val items = arrayOf(MOTORCYCLE_REPAIR_SERVICE, CAR_REPAIR_SERVICE, CAR_MOTORCYCLE_REPAIR_SERVICE)
        val adapter = ArrayAdapter(
            this@AddNewShopActivity,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
        binding.spinnerCategory.adapter = adapter
        shop.service = MOTORCYCLE_REPAIR_SERVICE
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                arg0: AdapterView<*>?,
                arg1: View?,
                arg2: Int,
                arg3: Long
            ) {
                shop.service = binding.spinnerCategory.selectedItem.toString()
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
    }

    private fun pickImage() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                pickImageFromGalleryForResult.launch(pickIntent)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(
                    String.format(
                        "package:%s", this.packageName
                    )
                )
                requestManageStoragePermission.launch(intent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImageFromGalleryForResult.launch(pickIntent)
            } else {
                requestReadPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun showProgressbar() {
        binding.pbLoading.visibility = View.VISIBLE
    }

    private fun hideProgressbar() {
        binding.pbLoading.visibility = View.GONE
    }

    private fun uploadShopImage() {
        val iBase64 = imageBase64
        if (iBase64 != null) {
            imageApi.uploadPhoto(Utils.createRequestBodyForImage(iBase64))
                .enqueue(object : Callback<JsonObject?> {
                    override fun onResponse(
                        call: Call<JsonObject?>,
                        response: Response<JsonObject?>
                    ) {
                        when {
                            response.isSuccessful -> {
                                response.body()?.let {
                                    val json = JSONObject(response.body().toString()).getJSONObject("image")
                                    val file = JSONObject(json.toString()).getJSONObject("file")
                                    val resource = JSONObject(file.toString()).getJSONObject("resource")
                                    val chain = JSONObject(resource.toString()).getJSONObject("chain")
                                    imageUrl = JSONObject(chain.toString()).getString("image")
                                    Log.e("imageLink", imageUrl.toString())
                                    upLoadShopInfo()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@AddNewShopActivity,
                            t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        hideProgressbar()
                        Utils.showShortToast(this@AddNewShopActivity, "Fail to upload avatar")
                        t.printStackTrace()
                    }
                })
        } else {
            upLoadShopInfo()
        }
    }

    private fun upLoadShopInfo() {
        with(binding) {
            shop.name = edtName.text.toString()
            shop.address = edtAddress.text.toString()
            shop.phone = edtPhone.text.toString()
            shop.openTime = edtOpenTime.text.toString()
            shop.website = edtWebsite.text.toString()
            shop.owner = edtOwner.text.toString()
            shop.imageUrl = imageUrl
        }
        shop.location = HashMap<String, Any>().apply {
            location?.latitude?.let { lat ->
                location?.longitude?.let { lng ->
                    GeoLocation(lat, lng)
                }
            }?.let { geoLocation ->
                GeoFireUtils.getGeoHashForLocation(geoLocation)
            }?.let { geoHash ->
                put(GEO_HASH, geoHash)
            }
            location?.latitude?.let { it1 -> put(LATITUDE, it1) }
            location?.longitude?.let { it1 -> put(LONGITUDE, it1) }
        }
        shop.pendingApprove = true
        shop.created = false
        shop.lastModifiedTime = Calendar.getInstance().timeInMillis.toDouble()
        FirebaseManager.getAuth()?.uid?.let { uid ->
            shop.id = uid
            db.collection(SHOP_PENDING_COLLECTION)
                .document(uid)
                .set(shop)
                .addOnSuccessListener {
                    db.collection(SHOP_COLLECTION)
                        .document(uid)
                        .update(
                            "pendingApprove", true,
                            "created", false
                        )
                    hideProgressbar()
                    startActivity(Intent(this@AddNewShopActivity, ShopMainActivity::class.java).apply {
                        putExtra(EXTRA_CREATED_SHOP, "created")
                    })
                    finish()
                }
                .addOnFailureListener { e ->
                    hideProgressbar()
                    Utils.showShortToast(
                        this@AddNewShopActivity, getString(R.string.fail_to_create_shop)
                    )
                    Log.e(ContentValues.TAG, "Error adding document", e)
                }
        }
    }
}
