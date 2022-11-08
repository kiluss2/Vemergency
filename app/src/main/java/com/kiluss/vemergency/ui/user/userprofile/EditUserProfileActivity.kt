package com.kiluss.vemergency.ui.user.userprofile

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
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
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import com.kiluss.vemergency.network.api.RetrofitClient
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.IMAGE_API_URL
import com.kiluss.vemergency.constant.MAX_WIDTH_IMAGE
import com.kiluss.vemergency.constant.USER_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.ActivityEditUserProfileBinding
import com.kiluss.vemergency.network.api.ImageService
import com.kiluss.vemergency.utils.URIPathHelper
import com.kiluss.vemergency.utils.Utils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class EditUserProfileActivity : AppCompatActivity() {

    private var imageUrl: String? = null
    private var imageBase64: String? = null
    private var user = User()
    private val db = Firebase.firestore
    private lateinit var binding: ActivityEditUserProfileBinding
    private lateinit var imageApi: ImageService
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
            binding.ivProfile.setImageBitmap(imageBitmap)
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
        binding = ActivityEditUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        getUserData()
        imageApi = RetrofitClient.getInstance(this).getClientUnAuthorize(IMAGE_API_URL).create(ImageService::class.java)
    }

    private fun setupView() {
        with(binding) {
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    val birthday = "$year-${monthOfYear + 1}-$dayOfMonth"
                    binding.tvBirthDayPicker.text = birthday
                    user.birthday = birthday
                }

            binding.tvBirthDayPicker.setOnClickListener {
                setUpDatePicker(dateSetListener)
            }

            btnSave.setOnClickListener {
                showProgressbar()
                uploadImage()
            }
            ivProfile.setOnClickListener {
                pickImage()
            }
        }
    }

    private fun upLoadUserInfo() {
        with(binding) {
            user.fullName = edtFullName.text.toString()
            user.address = edtAddress.text.toString()
            user.phone = edtPhoneNumber.text.toString()
            imageUrl?.let { user.imageUrl = it }
            user.lastModifiedTime = android.icu.util.Calendar.getInstance().timeInMillis.toDouble()
            FirebaseManager.getAuth()?.uid?.let {
                db.collection(USER_COLLECTION)
                    .document(it)
                    .set(user)
                    .addOnSuccessListener {
                        hideProgressbar()
                        Utils.showShortToast(this@EditUserProfileActivity, "Edit successful")
                        finish()
                    }
                    .addOnFailureListener {
                        hideProgressbar()
                        Utils.showShortToast(this@EditUserProfileActivity, "Fail to update profile")
                        Log.e(ContentValues.TAG, "Error adding document")
                    }
            }
        }
    }

    private fun uploadImage() {
        val iBase64 = imageBase64
        if (iBase64 != null) {
            imageApi.upload(Utils.createRequestBodyForImage(iBase64))
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
                                    upLoadUserInfo()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@EditUserProfileActivity,
                            t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        hideProgressbar()
                        Utils.showShortToast(this@EditUserProfileActivity, "Fail to upload avatar")
                        t.printStackTrace()
                    }
                })
        } else {
            upLoadUserInfo()
        }
    }

    private fun setUpDatePicker(dateSetListener: DatePickerDialog.OnDateSetListener?) {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val day = Calendar.getInstance().get(Calendar.DATE)
        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener, year, month, day
        )
        datePickerDialog.show()
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

    private fun getUserData() {
        FirebaseManager.getAuth()?.uid?.let { uid ->
            binding.tvEmail.text = FirebaseManager.getAuth()?.currentUser?.email
            db.collection(USER_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        documentSnapshot.toObject<User>()?.let { result ->
                            user = result
                            user.fullName?.let {
                                binding.edtFullName.setText(it)
                            }
                            user.birthday?.let {
                                binding.tvBirthDayPicker.text = it
                            }
                            user.address?.let {
                                binding.edtAddress.setText(it)
                            }
                            user.phone?.let {
                                binding.edtPhoneNumber.setText(it)
                            }
                            Glide.with(this@EditUserProfileActivity)
                                .load(user.imageUrl)
                                .placeholder(R.drawable.ic_account_avatar)
                                .centerCrop()
                                .into(binding.ivProfile)
                        }
                    }
                    hideProgressbar()
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Utils.showShortToast(this@EditUserProfileActivity, "Fail to get user information")
                    Log.e("Main Activity", exception.message.toString())
                }
            if (FirebaseManager.getAuth()?.uid == null) {
                hideProgressbar()
                Utils.showShortToast(this@EditUserProfileActivity, "Fail to get auth")
            }
        }
    }
}
