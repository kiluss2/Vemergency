package com.kiluss.vemergency.ui.userprofile

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.TEMP_IMAGE
import com.kiluss.vemergency.constant.USER_NODE
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.ActivityEditUserProfileBinding
import com.kiluss.vemergency.utils.URIPathHelper
import com.kiluss.vemergency.utils.Utils
import java.io.File
import java.util.Calendar

class EditUserProfileActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private var user = User()
    private lateinit var binding: ActivityEditUserProfileBinding
    private var uid: String? = null

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
            val imageBitmap = getFileImageBitmap(file)
            binding.ivProfile.setImageBitmap(imageBitmap)
            imageUri = intent?.data
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

        setupFirebase()
        setupView()
        getUserData()
    }

    private fun setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference(USER_NODE)
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid
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
                user.fullName = edtFullName.text.toString()
                user.address = edtAddress.text.toString()
                user.phone = edtPhoneNumber.text.toString()
                val uid = auth.currentUser?.uid
                uid?.let {
                    databaseReference.child(uid).setValue(user).addOnCompleteListener {
                        if (it.isSuccessful) {
                            // upload avatar picture
                            uploadAvatar()
                        } else {
                            hideProgressbar()
                            Utils.showShortToast(this@EditUserProfileActivity, "Fail to update profile")
                            it.exception?.printStackTrace()
                        }
                    }
                }
            }
            ivProfile.setOnClickListener {
                pickImage()
            }
        }
    }

    private fun uploadAvatar() {
        if (imageUri != null) {
            storageReference =
                FirebaseStorage.getInstance().getReference(USER_NODE + "/" + auth.currentUser?.uid)
            storageReference.putFile(imageUri!!).addOnCompleteListener {
                hideProgressbar()
                Utils.showShortToast(this@EditUserProfileActivity, "Edit successful")
                finish()
            }.addOnFailureListener {
                hideProgressbar()
                Utils.showShortToast(this@EditUserProfileActivity, "Fail to upload avatar")
                it.printStackTrace()
            }
        } else {
            hideProgressbar()
            Utils.showShortToast(this@EditUserProfileActivity, "Edit successful")
            finish()
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

    private fun getFileImageBitmap(imgFile: File): Bitmap {
        return BitmapFactory.decodeFile(imgFile.absolutePath)
    }

    private fun showProgressbar() {
        binding.pbLoading.visibility = View.VISIBLE
    }

    private fun hideProgressbar() {
        binding.pbLoading.visibility = View.GONE
    }

    private fun getUserData() {
        uid?.let { uid ->
            databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let { userDb ->
                        user = userDb
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
                        binding.tvEmail.text = auth.currentUser?.email
                    }
                    val localFile = File("$cacheDir/$TEMP_IMAGE/${auth.currentUser?.uid}.jpg")
                    if (localFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        Glide.with(this@EditUserProfileActivity)
                            .load(bitmap)
                            .placeholder(R.drawable.ic_account_avatar)
                            .into(binding.ivProfile)
                    }
                    hideProgressbar()
                }

                override fun onCancelled(error: DatabaseError) {
                    hideProgressbar()
                    Utils.showShortToast(this@EditUserProfileActivity, "Fail to get user information")
                    Log.e("Main Activity", error.message)
                }
            })
        }
    }
}
