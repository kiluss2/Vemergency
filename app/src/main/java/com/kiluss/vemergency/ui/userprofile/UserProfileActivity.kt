package com.kiluss.vemergency.ui.userprofile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import com.kiluss.vemergency.databinding.ActivityUserProfileBinding
import com.kiluss.vemergency.utils.Utils
import java.io.File


class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private var user: User? = null
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupFirebase()
        getUserData()
    }

    private fun getUserData() {
        uid?.let { uid ->
            databaseReference.child(uid).addValueEventListener(object : ValueEventListener {
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
                        binding.tvEmail.text = auth.currentUser?.email
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
        storageReference = FirebaseStorage.getInstance().reference.child(USER_NODE + "/" + auth.currentUser?.uid)
        File("$cacheDir/$TEMP_IMAGE").mkdirs()
        val localFile = File("$cacheDir/$TEMP_IMAGE/${auth.currentUser?.uid}.jpg")
        localFile.createNewFile()
        storageReference
            .getFile(localFile)
            .addOnCompleteListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                Glide.with(this)
                    .load(bitmap)
                    .placeholder(R.drawable.ic_account_avatar)
                    .into(binding.ivProfile)
            }.addOnFailureListener {
                it.printStackTrace()
            }
    }

    private fun setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference(USER_NODE)
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid
    }

    private fun setupView() {
        with(binding) {
            ivEdit.setOnClickListener {
                startActivity(Intent(this@UserProfileActivity, EditUserProfileActivity::class.java))
            }
            btnLogout.setOnClickListener {
                auth.signOut() //End user session
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
