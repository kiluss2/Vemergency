package com.kiluss.vemergency.ui.admin.approve

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_SHOP_DETAIL
import com.kiluss.vemergency.constant.EXTRA_SHOP_LOCATION
import com.kiluss.vemergency.constant.EXTRA_SHOP_PENDING
import com.kiluss.vemergency.constant.HTTP_PREFIX
import com.kiluss.vemergency.constant.SEND_NOTI_API_URL
import com.kiluss.vemergency.constant.SHOP_COLLECTION
import com.kiluss.vemergency.constant.SHOP_PENDING_COLLECTION
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ActivityApproveShopBinding
import com.kiluss.vemergency.network.api.ApiService
import com.kiluss.vemergency.network.api.RetrofitClient
import com.kiluss.vemergency.ui.admin.main.AdminMainViewModel
import com.kiluss.vemergency.ui.user.navigation.NavigationActivity
import com.kiluss.vemergency.utils.Utils
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApproveShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApproveShopBinding

    // view model ktx
    private val viewModel: AdminMainViewModel by viewModels()
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApproveShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()
        observeViewModel()
        setShopInfo()
        intent.getStringExtra(EXTRA_SHOP_DETAIL)?.let {
            binding.ivApprove.visibility = View.GONE
            binding.ivDecline.visibility = View.GONE
        }
    }

    private fun setShopInfo() {
        intent.getParcelableExtra<Shop>(EXTRA_SHOP_PENDING)?.let { shop ->
            shop.name?.let {
                binding.tvShopName.text = it
            }
            shop.phone?.let {
                binding.tvPhoneNumber.text = it
            }
            shop.address?.let {
                binding.tvAddress.text = it
            }
            shop.openTime?.let {
                binding.tvOpenTime.text = it
            }
            shop.website?.let {
                binding.tvWebsite.text = it
            }
            shop.owner?.let {
                binding.tvOwner.text = it
            }
            shop.service?.let {
                binding.tvService.text = it
            }
            Glide.with(this@ApproveShopActivity)
                .load(shop.imageUrl)
                .placeholder(R.drawable.login_background)
                .centerCrop()
                .into(binding.ivCover)
            binding.btnGetLocation.setOnClickListener {
                startActivity(Intent(this@ApproveShopActivity, NavigationActivity::class.java).apply {
                    putExtra(EXTRA_SHOP_LOCATION, shop)
                })
            }
            binding.ivApprove.setOnClickListener {
                showProgressbar()
                shop.pendingApprove = false
                shop.created = true
                approveShop(shop)
            }
            binding.ivDecline.setOnClickListener {
                showProgressbar()
                declineShop(shop)
            }
        }
    }

    private fun approveShop(shop: Shop) {
        shop.id?.let { uid ->
            db.collection(SHOP_COLLECTION)
                .document(uid)
                .set(shop)
                .addOnSuccessListener {
                    shop.fcmToken?.let {
                        sendAcceptNoti(it)
                    }
                    deleteShopPending(uid)
                }
                .addOnFailureListener { e ->
                    hideProgressbar()
                    Utils.showShortToast(
                        this@ApproveShopActivity, getString(R.string.fail_to_create_shop)
                    )
                    Log.e(ContentValues.TAG, "Error adding document", e)
                }
        }
    }

    private fun deleteShopPending(uid: String) {
        db.collection(SHOP_PENDING_COLLECTION)
            .document(uid)
            .delete()
            .addOnSuccessListener {
                hideProgressbar()
                finish()
            }
            .addOnFailureListener { e ->
                hideProgressbar()
                Utils.showShortToast(
                    this@ApproveShopActivity, getString(R.string.fail_to_create_shop)
                )
                Log.e(ContentValues.TAG, "Error adding document", e)
            }
    }

    private fun declineShop(shop: Shop) {
        shop.id?.let { id ->
            db.collection(SHOP_PENDING_COLLECTION)
                .document(id)
                .delete()
                .addOnSuccessListener {
                    db.collection(SHOP_COLLECTION)
                        .document(id)
                        .update("pendingApprove", false)
                    shop.fcmToken?.let {
                        sendRejectNoti(it)
                    }
                }
                .addOnFailureListener { e ->
                    hideProgressbar()
                    Utils.showShortToast(
                        this@ApproveShopActivity, getString(R.string.fail_to_create_shop)
                    )
                    Log.e(ContentValues.TAG, "Error adding document", e)
                }
        }
    }

    private fun setUpView() {
        with(binding) {
            tvPhoneNumber.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this@ApproveShopActivity,
                        android.Manifest.permission.CALL_PHONE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@ApproveShopActivity, arrayOf(android.Manifest.permission.CALL_PHONE),
                        0
                    )
                } else {
                    val alertDialog = AlertDialog.Builder(this@ApproveShopActivity)
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
            tvWebsite.setOnClickListener {
                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(HTTP_PREFIX + tvWebsite.text.toString()))
                startActivity(urlIntent)
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            progressBarStatus.observe(this@ApproveShopActivity) {
                if (it) {
                    showProgressbar()
                } else {
                    hideProgressbar()
                }
            }
        }
    }

    private fun sendAcceptNoti(fcmToken: String) {
        val request = JSONObject()
        request.put("token", fcmToken)
        RetrofitClient.getInstance(this).getClientUnAuthorize(SEND_NOTI_API_URL)
            .create(ApiService::class.java)
            .sendNotiAcceptShop(request.toString().toRequestBody())
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    when {
                        response.isSuccessful -> {
                            Log.e("acceptShop", response.body().toString())
                        }
                        else -> {
                            Log.e("acceptShop", "fail send notification")
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("failSendNotiAcceptShop", t.toString())
                    t.printStackTrace()
                }
            })
    }

    private fun sendRejectNoti(fcmToken: String) {
        val request = JSONObject()
        request.put("token", fcmToken)
        RetrofitClient.getInstance(this).getClientUnAuthorize(SEND_NOTI_API_URL)
            .create(ApiService::class.java)
            .sendNotiRejectShop(request.toString().toRequestBody())
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    when {
                        response.isSuccessful -> {
                            Log.e("rejectShop", response.body().toString())
                            finish()
                        }
                        else -> {
                            Log.e("rejectShop", "fail send notification")
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("failSendNotiRejectShop", t.toString())
                    t.printStackTrace()
                    finish()
                }
            })
    }

    private fun showProgressbar() {
        binding.pbLoading.visibility = View.VISIBLE
    }

    private fun hideProgressbar() {
        binding.pbLoading.visibility = View.GONE
    }
}
