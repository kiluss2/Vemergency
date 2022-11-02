package com.kiluss.vemergency.ui.shop.main

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_SHOP_LOCATION
import com.kiluss.vemergency.constant.HTTP_PREFIX
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.FragmentMyShopBinding
import com.kiluss.vemergency.ui.shop.addshop.AddNewShopActivity
import com.kiluss.vemergency.ui.user.navigation.NavigationActivity

class MyShopFragment : Fragment() {

    private var _binding: FragmentMyShopBinding? = null
    private val binding get() = _binding!!

    // view model ktx
    private val viewModel: ShopMainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        observeViewModel()
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
            btnCreateShop.setOnClickListener {
                requireActivity().startActivity(
                    Intent(requireActivity(), AddNewShopActivity::class.java)
                )
            }
            tvWebsite.setOnClickListener {
                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(HTTP_PREFIX + tvWebsite.text.toString()))
                startActivity(urlIntent)
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
                if (FirebaseManager.getCurrentUser() == null) {
                    binding.clMain.visibility = View.GONE
                    binding.tvCreateToManage.visibility = View.VISIBLE
                    binding.tvCreateToManage.text = "Something went wrong"
                } else {
                    if (shop != null) {
                        if (shop.pendingApprove == true) {
                            binding.tvPendingApprove.visibility = View.VISIBLE
                            binding.tvPendingApprove.setShadowLayer(2f, 2f, 2f, Color.WHITE);
                        }
                        binding.clCreateShop.visibility = View.GONE
                        binding.clMain.visibility = View.VISIBLE
                        binding.tvCreateToManage.visibility = View.GONE
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
                        Glide.with(this@MyShopFragment)
                            .load(shop.imageUrl)
                            .placeholder(R.drawable.login_background)
                            .centerCrop()
                            .into(binding.ivCover)
                    } else {
                        binding.clMain.visibility = View.GONE
                        binding.clCreateShop.visibility = View.VISIBLE
                        binding.tvCreateToManage.visibility = View.VISIBLE
                        binding.tvCreateToManage.text = getString(R.string.create_to_manage_your_shop)
                    }
                }
                binding.btnGetLocation.setOnClickListener {
                    startActivity(Intent(this@MyShopFragment.requireContext(), NavigationActivity::class.java).apply {
                        putExtra(EXTRA_SHOP_LOCATION, shop)
                    })
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
        viewModel.getShopInfo()
    }
}
