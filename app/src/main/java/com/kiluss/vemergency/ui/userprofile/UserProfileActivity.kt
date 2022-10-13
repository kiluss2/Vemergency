package com.kiluss.vemergency.ui.userprofile

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.AVATAR
import com.kiluss.vemergency.constant.TEMP_IMAGE
import com.kiluss.vemergency.constant.USER_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.ActivityUserProfileBinding
import com.kiluss.vemergency.utils.Utils
import java.io.File

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private var user: User? = null
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseManager.init()
        setupView()
    }

    private fun getUserData() {
        FirebaseManager.getAuth()?.uid?.let {
            binding.tvEmail.text = FirebaseManager.getCurrentUser()?.email
            db.collection(USER_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    documentSnapshot.toObject<User>()?.let { result ->
                        user = result
                        user?.let { user ->
                            user.fullName?.let {
                                binding.tvFullName.text = it
                            }
                            user.birthday?.let {
                                binding.tvBirthDay.text = it
                            }
                            user.address?.let {
                                binding.tvAddress.text = it
                            }
                            user.phone?.let {
                                binding.tvPhoneNumber.text = it
                            }
                        }
                        hideProgressbar()
                        getAvatar()
                    }
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Utils.showShortToast(this@UserProfileActivity, "Fail to get user information")
                    Log.e("Main Activity", exception.message.toString())
                }
        }
    }

    private fun getAvatar() {
        File("$cacheDir/$TEMP_IMAGE").mkdirs()
        val localFile = File("$cacheDir/$TEMP_IMAGE/$AVATAR.jpg")
        if (localFile.exists()) {
            localFile.delete()
        }
        localFile.createNewFile()
        FirebaseManager.getUserAvatarStorageReference()
            .getFile(localFile)
            .addOnCompleteListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                bitmap?.let {
                    Glide.with(applicationContext)
                        .load(bitmap)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.ivProfile)
                }
            }.addOnFailureListener {
                it.printStackTrace()
            }
    }

    private fun setupView() {
        with(binding) {
            ivEdit.setOnClickListener {
                startActivity(Intent(this@UserProfileActivity, EditUserProfileActivity::class.java))
            }
            btnLogout.setOnClickListener {
                FirebaseManager.getAuth()?.signOut() //End user session
                FirebaseManager.logout()
                finish()
            }
            tvPhoneNumber.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this@UserProfileActivity,
                        android.Manifest.permission.CALL_PHONE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@UserProfileActivity, arrayOf(android.Manifest.permission.CALL_PHONE),
                        0
                    )
                } else {
                    val alertDialog = AlertDialog.Builder(this@UserProfileActivity)

                    alertDialog.apply {
                        //setIcon(R.drawable.ic_hello)
                        setTitle("Make a phone call?")
                        setMessage("Do you want to make a phone call?")
                        setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                            // make phone call
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tvPhoneNumber.text))
                            startActivity(intent)
                        }
                        setNegativeButton("No") { _, _ ->
                        }
                    }.create().show()
                }
            }
        }
    }

    private fun showProgressbar() {
        binding.pbLoading.visibility = View.VISIBLE
    }

    private fun hideProgressbar() {
        binding.pbLoading.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        getUserData()
    }
}
