package com.kiluss.vemergency.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kiluss.vemergency.databinding.FragmentMyShopBinding

class MyShopFragment : Fragment() {

    private var _binding: FragmentMyShopBinding? = null
    private val binding get() = _binding!!

    // view model ktx
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        observeViewModel()
        viewModel.getShopData()
    }

    private fun setUpView() {
        with(binding) {
            tvPhoneNumber.setOnClickListener {
                if (this@MyShopFragment.context?.let {
                        ContextCompat.checkSelfPermission(
                            it,
                            android.Manifest.permission.CALL_PHONE
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MyShopFragment.context as Activity, arrayOf(android.Manifest.permission.CALL_PHONE),
                        0
                    )
                } else {
                    val alertDialog = AlertDialog.Builder(context)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        with(viewModel) {
            progressBarStatus.observe(viewLifecycleOwner) {
                if (it) {
                    showProgressbar()
                } else {
                    hideProgressbar()
                }
            }
            shop.observe(viewLifecycleOwner) { shop ->
                shop?.let {
                    it.name?.let {
                        binding.tvShopName.text = it
                    }
                    it.phone?.let {
                        binding.tvPhoneNumber.text = it
                    }
                    it.address?.let {
                        binding.tvAddress.text = it
                    }
                    it.openTime?.let {
                        binding.tvOpenTime.text = it
                    }
                    it.website?.let {
                        binding.tvWebsite.text = it
                    }
                }
            }
            shopImage.observe(viewLifecycleOwner) {
                activity?.let { it1 ->
                    Glide.with(it1)
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.ivCover)
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
}
