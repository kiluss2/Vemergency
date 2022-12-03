package com.kiluss.vemergency.ui.shop.edit

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
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
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_SHOP_DETAIL
import com.kiluss.vemergency.constant.IMAGE_API_URL
import com.kiluss.vemergency.constant.MAX_WIDTH_IMAGE
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ActivityEditShopProfileBinding
import com.kiluss.vemergency.network.api.ApiService
import com.kiluss.vemergency.network.api.RetrofitClient
import com.kiluss.vemergency.utils.URIPathHelper
import com.kiluss.vemergency.utils.Utils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditShopProfileActivity : AppCompatActivity() {
    private var imageUrl: String? = null
    private var imageBase64: String? = null
    private var shop = Shop()
    private val db = Firebase.firestore
    private lateinit var binding: ActivityEditShopProfileBinding
    private lateinit var imageApi: ApiService
    private val requestManageStoragePermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val pickImageFromGalleryForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val imagePath =
                intent?.data?.let { URIPathHelper().getPath(this, it) } ?: ""
            // handle image from gallery
            val file = File(imagePath)
            val imageBitmap = Utils.getResizedBitmap(file, MAX_WIDTH_IMAGE)
            binding.ivCover.setImageBitmap(imageBitmap)
            imageBase64 = Utils.encodeImageToBase64String(imageBitmap)
        }
    }
    private val requestReadPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    this, getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditShopProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        getShopData()
        imageApi = RetrofitClient.getInstance(this).getClientUnAuthorize(IMAGE_API_URL).create(ApiService::class.java)
    }

    private fun getShopData() {
        intent.getParcelableExtra<Shop>(EXTRA_SHOP_DETAIL)?.let {
            shop = it
            with(shop) {
                name?.let {
                    binding.edtShopName.setText(it)
                }
                owner?.let {
                    binding.edtOwnerName.setText(it)
                }
                address?.let {
                    binding.edtAddress.setText(it)
                }
                phone?.let {
                    binding.edtPhoneNumber.setText(it)
                }
                openTime?.let {
                    binding.edtOpenTime.setText(it)
                }
                website?.let {
                    binding.edtWebsite.setText(it)
                }
                Glide.with(this@EditShopProfileActivity)
                    .load(imageUrl)
                    .placeholder(R.drawable.login_background)
                    .centerCrop()
                    .into(binding.ivCover)
            }
        }
    }

    private fun setupView() {
        with(binding) {
            pbLoading.visibility = View.INVISIBLE
            btnSave.setOnClickListener {
                if (edtShopName.text.isEmpty()) {
                    edtShopName.error = getString(R.string.please_fill_in_this_field)
                } else if (edtAddress.text.isEmpty()) {
                    edtAddress.error = getString(R.string.please_fill_in_this_field)
                } else if (edtPhoneNumber.text.isEmpty()) {
                    edtPhoneNumber.error = getString(R.string.please_fill_in_this_field)
                } else {
                    showProgressbar()
                    db.collection(SHOP_COLLECTION)
                        .whereEqualTo("email", edtPhoneNumber.text.toString())
                        .get()
                        .addOnSuccessListener {
                            if (it.size() != 0) {
                                edtPhoneNumber.error = getString(R.string.phone_number_is_existed)
                                hideProgressbar()
                            } else {
                                uploadImage()
                            }
                        }
                }
            }
            ivCover.setOnClickListener {
                pickImage()
            }
        }
    }

    private fun upLoadShopInfo() {
        with(binding) {
            shop.name = edtShopName.text.toString()
            shop.owner = edtOwnerName.text.toString()
            shop.address = edtAddress.text.toString()
            shop.phone = edtPhoneNumber.text.toString()
            shop.openTime = edtOpenTime.text.toString()
            shop.website = edtWebsite.text.toString()
            imageUrl?.let { shop.imageUrl = it }
            shop.lastModifiedTime = android.icu.util.Calendar.getInstance().timeInMillis.toDouble()
            FirebaseManager.getAuth()?.uid?.let {
                db.collection(SHOP_COLLECTION)
                    .document(it)
                    .set(shop)
                    .addOnSuccessListener {
                        hideProgressbar()
                        Utils.showShortToast(this@EditShopProfileActivity, "Edit successful")
                        finish()
                    }
                    .addOnFailureListener {
                        hideProgressbar()
                        Utils.showShortToast(this@EditShopProfileActivity, "Fail to update profile")
                        Log.e(ContentValues.TAG, "Error adding document")
                    }
            }
        }
    }

    private fun uploadImage() {
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
                            this@EditShopProfileActivity,
                            t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        hideProgressbar()
                        Utils.showShortToast(this@EditShopProfileActivity, "Fail to upload avatar")
                        t.printStackTrace()
                    }
                })
        } else {
            upLoadShopInfo()
        }
    }

    private fun pickImage() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                pickImageFromGalleryForResult.launch(pickIntent)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(
                    String.format(
                        "package:%s",
                        this.packageName
                    )
                )
                requestManageStoragePermission.launch(intent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
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
}