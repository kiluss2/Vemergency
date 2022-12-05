package com.kiluss.vemergency.ui.shop.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_CHANGE_PASSWORD
import com.kiluss.vemergency.constant.EXTRA_SHOP_DETAIL
import com.kiluss.vemergency.constant.LOGIN_FRAGMENT_EXTRA
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.FragmentShopSettingBinding
import com.kiluss.vemergency.ui.login.LoginActivity
import com.kiluss.vemergency.ui.shop.edit.EditShopProfileActivity

class ShopSettingFragment : Fragment() {
    private var _binding: FragmentShopSettingBinding? = null
    private val binding get() = _binding!!

    // view model ktx
    private val viewModel: ShopMainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentShopSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupView()
        viewModel.getShopInfo()
    }

    private fun setupView() {
        with(binding) {
            tvSignOut.setOnClickListener {
                createSignOutDialog()
            }
            rlPersonalDetail.setOnClickListener {
                viewModel.navigateToHome()
            }
            tvEditProfile.setOnClickListener {
                startActivity(Intent(activity, EditShopProfileActivity::class.java).apply {
                    putExtra(
                        EXTRA_SHOP_DETAIL,
                        viewModel.getShopData()
                    )
                })
            }
            tvChangePassword.setOnClickListener {
                startActivity(Intent(activity, LoginActivity::class.java).apply {
                    putExtra(
                        LOGIN_FRAGMENT_EXTRA,
                        EXTRA_CHANGE_PASSWORD
                    )
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        with(viewModel) {
            avatarBitmap.observe(viewLifecycleOwner) {
                if (FirebaseManager.getCurrentUser() != null) {
                    it?.let {
                        Glide.with(this@ShopSettingFragment)
                            .load(it)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .dontAnimate()
                            .into(binding.profileCircleImageView)
                    }
                }
            }
            shop.observe(viewLifecycleOwner) {
                with(binding) {
                    FirebaseManager.getAuth()?.currentUser?.email?.let {
                        usernameTextView.text = it
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseManager.getCurrentUser() == null) {
            Glide.with(this)
                .load(R.drawable.ic_account_avatar)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .dontAnimate()
                .into(binding.profileCircleImageView)
        }
    }

    private fun createSignOutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Sign out")
        builder.setMessage("Do you want to sign out")

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            viewModel.signOut()
            Glide.with(this@ShopSettingFragment)
                .load(R.drawable.ic_account_avatar)
                .into(binding.profileCircleImageView)
            startActivity(Intent(this@ShopSettingFragment.requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }

        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
        }
        builder.show()
    }
}
