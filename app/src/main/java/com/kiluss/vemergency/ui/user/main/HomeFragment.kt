package com.kiluss.vemergency.ui.user.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_LAUNCH_MAP
import com.kiluss.vemergency.constant.EXTRA_USER_PROFILE
import com.kiluss.vemergency.constant.LOGIN_FRAGMENT_EXTRA
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.FragmentHomeBinding
import com.kiluss.vemergency.ui.login.LoginActivity
import com.kiluss.vemergency.ui.user.navigation.NavigationActivity
import java.text.MessageFormat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // view model ktx
    private val viewModel: MainViewModel by activityViewModels()
    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseManager.init()
        setUpView()
        observeViewModel()
        viewModel.getUserInfo()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener(requireActivity()) { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        viewModel.getNearByShop(location, 10)
                    }
                }
        }
    }

    private fun setUpView() {
        val animation: Animation = AlphaAnimation(1f, 0f) // Change alpha from fully visible to invisible

        animation.duration = 800
        animation.interpolator = LinearInterpolator() // do not alter animation rate
        animation.repeatCount = Animation.INFINITE // Repeat animation infinitely
        animation.repeatMode = Animation.REVERSE
        binding.ivNearBy.startAnimation(animation)
        binding.btnFind.setOnClickListener {
            startActivity(Intent(activity, NavigationActivity::class.java).apply {
                putExtra(EXTRA_LAUNCH_MAP, "")
            })
        }
        binding.ivAccount.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java).apply {
                putExtra(
                    LOGIN_FRAGMENT_EXTRA,
                    EXTRA_USER_PROFILE
                )
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        with(viewModel) {
            userInfo.observe(viewLifecycleOwner) { user ->
                with(binding) {
                    if (user.fullName != null) {
                        tvEmail.text = "Welcome ${user.fullName}"
                    } else {
                        tvEmail.text = getString(R.string.app_name)
                    }
                    Glide.with(this@HomeFragment)
                        .load(user.imageUrl)
                        .placeholder(R.drawable.ic_account_avatar)
                        .centerCrop()
                        .into(binding.ivAccount)
                }
            }
            nearByShopCount.observe(viewLifecycleOwner) { place ->
                with(binding) {
                    tvNearBy.text = MessageFormat.format(
                        resources.getText(R.string.text_found_near_by).toString(),
                        place
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseManager.getCurrentUser() == null) {
            Glide.with(this@HomeFragment)
                .load(R.drawable.ic_account_avatar)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .dontAnimate()
                .into(binding.ivAccount)
        }
    }
}
