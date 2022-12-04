package com.kiluss.vemergency.ui.admin.manage

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_SHOP_DETAIL
import com.kiluss.vemergency.constant.EXTRA_SHOP_LOCATION
import com.kiluss.vemergency.constant.HTTP_PREFIX
import com.kiluss.vemergency.data.model.Shop
import com.kiluss.vemergency.databinding.ActivityAdminManageShopBinding
import com.kiluss.vemergency.ui.shop.edit.EditShopProfileActivity
import com.kiluss.vemergency.ui.user.navigation.NavigationActivity

class AdminManageShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManageShopBinding
    private lateinit var shop: Shop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManageShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.getParcelableExtra<Shop>(EXTRA_SHOP_DETAIL)?.let {
            shop = it
        }
        setUpView()
    }

    private fun setUpView() {
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
        val rating = shop.rating
        if (rating != null) {
            binding.rbShopRating.rating = rating.toFloat()
            binding.rbShopRating.visibility = View.VISIBLE
            binding.tvReviewCount.visibility = View.VISIBLE
        } else {
            binding.rbShopRating.visibility = View.GONE
            binding.tvReviewCount.visibility = View.GONE
        }
        Glide.with(this@AdminManageShopActivity)
            .load(shop.imageUrl)
            .placeholder(R.drawable.login_background)
            .centerCrop()
            .into(binding.ivCover)
        binding.btnGetLocation.setOnClickListener {
            startActivity(Intent(this@AdminManageShopActivity, NavigationActivity::class.java).apply {
                putExtra(EXTRA_SHOP_LOCATION, shop)
            })
        }
        binding.tvPhoneNumber.setOnClickListener {
            if (this@AdminManageShopActivity.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        android.Manifest.permission.CALL_PHONE
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@AdminManageShopActivity, arrayOf(android.Manifest.permission.CALL_PHONE),
                    0
                )
            } else {
                val alertDialog = AlertDialog.Builder(this@AdminManageShopActivity)
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
        binding.tvWebsite.setOnClickListener {
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(HTTP_PREFIX + binding.tvWebsite.text.toString()))
            startActivity(urlIntent)
        }
        binding.ivEdit.setOnClickListener {
            startActivity(Intent(this, EditShopProfileActivity::class.java).apply {
                putExtra(EXTRA_SHOP_DETAIL, shop)
            })
        }
        binding.btnDelete.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@AdminManageShopActivity)
            alertDialog.apply {
                setIcon(R.drawable.ic_delete)
                setTitle(getString(R.string.delete_question))
                setMessage(getString(R.string.do_you_want_to_delete))
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
}