package com.kiluss.vemergency.ui.userprofile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kiluss.vemergency.constant.AVATAR
import com.kiluss.vemergency.constant.TEMP_IMAGE
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.ActivityUserProfileBinding
import com.kiluss.vemergency.utils.Utils
import java.io.File


class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        getUserData()
    }

    private fun getUserData() {
        FirebaseManager.getUid()?.let { uid ->
            FirebaseManager.getUserInfoDatabaseReference().addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
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
                        binding.tvEmail.text = FirebaseManager.getCurrentUser()?.email
                    }
                    hideProgressbar()
                    getAvatar()
                }

                override fun onCancelled(error: DatabaseError) {
                    hideProgressbar()
                    Utils.showShortToast(this@UserProfileActivity, "Fail to get user information")
                    Log.e("Main Activity", error.message)
                }
            })
        }
    }

    private fun getAvatar() {
        File("$cacheDir/$TEMP_IMAGE").mkdirs()
        val localFile = File("$cacheDir/$TEMP_IMAGE/$AVATAR.jpg")
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
                finish()
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
