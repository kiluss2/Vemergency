package com.kiluss.vemergency.ui.shop.addshop

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.LatLng
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ActivityAddNewShopBinding
import com.kiluss.vemergency.ui.shop.main.ShopMainActivity
import com.kiluss.vemergency.ui.user.navigation.PickLocationActivity
import com.kiluss.vemergency.utils.URIPathHelper
import com.kiluss.vemergency.utils.Utils
import java.io.File

class AddNewShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewShopBinding
    private var imageUri: Uri? = null
    private var shop = Shop()
    private var location: LatLng? = null
    private val db = Firebase.firestore
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
            val imageBitmap = getFileImageBitmap(file)
            binding.ivCoverPicked.setImageBitmap(imageBitmap)
            imageUri = intent?.data
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
                if (edtName.text.isEmpty()) {
                    edtName.error = "Name can not empty"
                }
                if (edtAddress.text.isEmpty()) {
                    edtAddress.error = "Address can not empty"
                }
                if (tvLocationPicked.text.isEmpty()) {
                    Utils.showShortToast(this@AddNewShopActivity, "Please choose location")
                }
                if (edtName.text.isNotEmpty() && edtAddress.text.isNotEmpty() && tvLocationPicked.text.isNotEmpty()) {
                    showProgressbar()
                    shop.name = edtName.text.toString()
                    shop.address = edtAddress.text.toString()
                    shop.phone = edtPhone.text.toString()
                    shop.openTime = edtOpenTime.text.toString()
                    shop.website = edtWebsite.text.toString()
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
                    shop.isPendingApprove = true
                    shop.isCreated = false
                    FirebaseManager.getAuth()?.uid?.let { uid ->
                        shop.uid = uid
                        db.collection(SHOP_PENDING_COLLECTION)
                            .document(uid)
                            .set(shop)
                            .addOnSuccessListener {
                                // upload shop image
                                uploadShopImage()
                                db.collection(SHOP_COLLECTION)
                                    .document(uid)
                                    .set(Shop().apply {
                                        isPendingApprove = true
                                        isCreated = false
                                    })
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

    private fun getFileImageBitmap(imgFile: File): Bitmap {
        return BitmapFactory.decodeFile(imgFile.absolutePath)
    }

    private fun showProgressbar() {
        binding.pbLoading.visibility = View.VISIBLE
    }

    private fun hideProgressbar() {
        binding.pbLoading.visibility = View.GONE
    }

    private fun uploadShopImage() {
        if (imageUri != null) {
            FirebaseManager.getShopImageStorageReference().putFile(imageUri!!).addOnCompleteListener {
                hideProgressbar()
                startActivity(Intent(this, ShopMainActivity::class.java).apply {
                    putExtra(EXTRA_CREATED_SHOP, "created")
                })
                finish()
            }.addOnFailureListener {
                hideProgressbar()
                Utils.showShortToast(this@AddNewShopActivity, "Fail to upload avatar")
                it.printStackTrace()
            }
        } else {
            hideProgressbar()
            Utils.showShortToast(this@AddNewShopActivity, "Edit successful")
            finish()
        }
    }
}
