package com.kiluss.vemergency.ui.user.main

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
import com.kiluss.vemergency.constant.EXTRA_USER_DETAIL
import com.kiluss.vemergency.constant.EXTRA_USER_PROFILE
import com.kiluss.vemergency.constant.LOGIN_FRAGMENT_EXTRA
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.FragmentSettingBinding
import com.kiluss.vemergency.ui.login.LoginActivity
import com.kiluss.vemergency.ui.user.userprofile.EditUserProfileActivity

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    // view model ktx
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupView()
        viewModel.getUserInfo()
    }

    private fun setupView() {
        with(binding) {
            tvSignOut.setOnClickListener {
                FirebaseManager.getAuth()?.currentUser?.let {
                    createSignOutDialog()
                }
            }
            rlPersonalDetail.setOnClickListener {
                startActivity(Intent(activity, LoginActivity::class.java).apply {
                    putExtra(
                        LOGIN_FRAGMENT_EXTRA,
                        EXTRA_USER_PROFILE
                    )
                })
            }
            tvEditProfile.setOnClickListener {
                startActivity(Intent(requireActivity(), EditUserProfileActivity::class.java).apply {
                    putExtra(EXTRA_USER_DETAIL, viewModel.getUserData())
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
            userInfo.observe(viewLifecycleOwner) { user ->
                with(binding) {
                    FirebaseManager.getAuth()?.currentUser?.email?.let {
                        usernameTextView.text = it
                    }
                    if (FirebaseManager.getAuth()?.currentUser != null) {
                        FirebaseManager.getAuth()?.currentUser?.email?.let {
                            usernameTextView.text = it
                        }
                    } else {
                        usernameTextView.text = getString(R.string.prompt_username)
                    }
                    Glide.with(this@SettingFragment)
                        .load(user.imageUrl)
                        .placeholder(R.drawable.ic_account_avatar)
                        .centerCrop()
                        .into(binding.profileCircleImageView)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            if (FirebaseManager.getCurrentUser() == null) {
                Glide.with(this@SettingFragment)
                    .load(R.drawable.ic_account_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .dontAnimate()
                    .into(binding.profileCircleImageView)
                tvProfileLabel.visibility = View.GONE
                tvEditProfile.visibility = View.GONE
                tvChangePassword.visibility = View.GONE
            } else {
                tvProfileLabel.visibility = View.VISIBLE
                tvEditProfile.visibility = View.VISIBLE
                tvChangePassword.visibility = View.VISIBLE
            }
        }
    }

    private fun createSignOutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Sign out")
        builder.setMessage("Do you want to sign out")

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            viewModel.signOut()
            Glide.with(this@SettingFragment)
                .load(R.drawable.ic_account_avatar)
                .into(binding.profileCircleImageView)
        }

        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
        }
        builder.show()
    }
}
