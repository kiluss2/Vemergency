package com.kiluss.vemergency.ui.shop

import android.Manifest
import android.app.Activity
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_CREATED_SHOP
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.ActivityAddNewShopBinding
import com.kiluss.vemergency.ui.main.MainActivity
import com.kiluss.vemergency.utils.URIPathHelper
import com.kiluss.vemergency.utils.Utils
import java.io.File

class AddNewShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewShopBinding
    private var imageUri: Uri? = null
    private var shop = Shop()
    private var user = User()

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
            binding.ivCoverPicked.setImageBitmap(imageBitmap)
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
        binding = ActivityAddNewShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
    }

    private fun setupView() {
        with(binding) {
            llCover.setOnClickListener {
                pickImage()
            }
            btnSubmit.setOnClickListener {
                showProgressbar()
                shop.name = edtName.text.toString()
                shop.address = edtAddress.text.toString()
                shop.phone = edtPhone.text.toString()
                shop.openTime = edtOpenTime.text.toString()
                shop.website = edtWebsite.text.toString()
                FirebaseManager.getUid()?.let { uid ->
                    FirebaseManager.getUserInfoDatabaseReference().addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.getValue(User::class.java)?.let { userDb ->
                                user = userDb
                                user.shop = shop
                                FirebaseManager.getUid()?.let {
                                    FirebaseManager.getUserInfoDatabaseReference().setValue(user).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            // upload shop image
                                            uploadShopImage()
                                        } else {
                                            hideProgressbar()
                                            Utils.showShortToast(this@AddNewShopActivity, "Fail to update profile")
                                            it.exception?.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            hideProgressbar()
                            Utils.showShortToast(this@AddNewShopActivity, "Fail to get user information")
                            Log.e("Main Activity", error.message)
                        }
                    })
                }
            }
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
                startActivity(Intent(this, MainActivity::class.java).apply {
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
