package com.kiluss.vemergency.ui.user.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_LAUNCH_MAP
import com.kiluss.vemergency.constant.EXTRA_SHOP_LOCATION
import com.kiluss.vemergency.constant.EXTRA_USER_PROFILE
import com.kiluss.vemergency.constant.LOGIN_FRAGMENT_EXTRA
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.FragmentHomeBinding
import com.kiluss.vemergency.ui.user.login.LoginActivity
import com.kiluss.vemergency.ui.user.navigation.NavigationActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // view model ktx
    private val viewModel: MainViewModel by activityViewModels()

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
    }

    private fun setUpView() {
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
            avatarBitmap.observe(viewLifecycleOwner) {
                if (FirebaseManager.getCurrentUser() != null) {
                    it?.let {
                        Glide.with(this@HomeFragment)
                            .load(it)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .dontAnimate()
                            .into(binding.ivAccount)
                    }
                }
            }
            userInfo.observe(viewLifecycleOwner) {
                with(binding) {
                    tvEmail.text = "Welcome ${it.fullName}"
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
