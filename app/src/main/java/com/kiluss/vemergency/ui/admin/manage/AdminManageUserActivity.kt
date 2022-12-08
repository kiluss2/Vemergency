package com.kiluss.vemergency.ui.admin.manage

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_USER_DETAIL
import com.kiluss.vemergency.constant.USER_COLLECTION
import com.kiluss.vemergency.data.model.User
import com.kiluss.vemergency.databinding.ActivityAdminManageUserBinding
import com.kiluss.vemergency.ui.user.userprofile.EditUserProfileActivity

class AdminManageUserActivity : AppCompatActivity() {
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                result.data?.getParcelableExtra<User>(EXTRA_USER_DETAIL)?.let {
                    user = it
                    setUpView()
                }
            }
        }
    private lateinit var binding: ActivityAdminManageUserBinding
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManageUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.getParcelableExtra<User>(EXTRA_USER_DETAIL)?.let {
            user = it
        }
        setUpView()
    }

    private fun setUpView() {
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
        user.userName?.let {
            binding.tvUserName.text = it
        }
        Glide.with(this@AdminManageUserActivity)
            .load(user.imageUrl)
            .placeholder(R.drawable.ic_account_avatar)
            .centerCrop()
            .into(binding.ivProfile)

        binding.tvPhoneNumber.setOnClickListener {
            if (this@AdminManageUserActivity.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        android.Manifest.permission.CALL_PHONE
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@AdminManageUserActivity, arrayOf(android.Manifest.permission.CALL_PHONE),
                    0
                )
            } else {
                val alertDialog = AlertDialog.Builder(this@AdminManageUserActivity)
                alertDialog.apply {
                    setIcon(R.drawable.ic_call)
                    setTitle(getString(R.string.make_a_phone_call))
                    setMessage(getString(R.string.do_you_want_phone_call))
                    setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int ->
                        // make phone call
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + binding.tvPhoneNumber.text))
                        startActivity(intent)
                    }
                    setNegativeButton(getString(R.string.no)) { _, _ ->
                    }
                }.create().show()
            }
        }
        binding.ivEdit.setOnClickListener {
            startForResult.launch(Intent(this, EditUserProfileActivity::class.java).apply {
                putExtra(EXTRA_USER_DETAIL, user)
            })
        }
        binding.btnDelete.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@AdminManageUserActivity)
            alertDialog.apply {
                setIcon(R.drawable.ic_delete)
                setTitle(getString(R.string.delete_question))
                setMessage(getString(R.string.do_you_want_to_delete))
                setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int ->
                    user.id?.let { it1 ->
                        Firebase.firestore.collection(USER_COLLECTION).document(it1).delete()
                        finish()
                    }
                }
                setNegativeButton(getString(R.string.no)) { _, _ ->
                }
            }.create().show()
        }
    }
}