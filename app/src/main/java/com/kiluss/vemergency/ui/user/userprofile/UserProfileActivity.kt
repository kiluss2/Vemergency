package com.kiluss.vemergency.ui.user.userprofile

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_USER_DETAIL
import com.kiluss.vemergency.constant.USER_COLLECTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.ActivityUserProfileBinding
import com.kiluss.vemergency.utils.Utils

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private var user: User? = null
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    private fun getUserData() {
        FirebaseManager.getAuth()?.uid?.let {
            binding.tvEmail.text = FirebaseManager.getCurrentUser()?.email
            db.collection(USER_COLLECTION)
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
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
                                Glide.with(this@UserProfileActivity)
                                    .load(user.imageUrl)
                                    .placeholder(R.drawable.ic_account_avatar)
                                    .centerCrop()
                                    .into(binding.ivProfile)
                            }
                        }
                    }
                    hideProgressbar()
                }
                .addOnFailureListener { exception ->
                    hideProgressbar()
                    Utils.showShortToast(this@UserProfileActivity, "Fail to get user information")
                    Log.e("UserProfileActivity", exception.message.toString())
                }
        }
        if (FirebaseManager.getAuth()?.uid == null) {
            hideProgressbar()
            Utils.showShortToast(this@UserProfileActivity, "Fail to get auth")
        }
    }

    private fun setupView() {
        with(binding) {
            ivEdit.setOnClickListener {
                startActivity(Intent(this@UserProfileActivity, EditUserProfileActivity::class.java).apply {
                    putExtra(EXTRA_USER_DETAIL, user)
                })
            }
            btnLogout.setOnClickListener {
                removeFcmToken()
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
                        setIcon(R.drawable.ic_call)
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

    private fun removeFcmToken() {
        FirebaseManager.getAuth()?.currentUser?.uid?.let { uid ->
            db.collection(Utils.getCollectionRole()).document(uid).update("fcmToken", "")
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
