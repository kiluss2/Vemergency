package com.kiluss.vemergency.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.kiluss.vemergency.constant.ROLE_NAN
import com.kiluss.vemergency.constant.SHARE_PREF_ROLE
import com.kiluss.vemergency.databinding.ActivityLoginBinding
import com.kiluss.vemergency.ui.launcher.PickRoleActivity
import com.kiluss.vemergency.utils.SharedPrefManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginPagerAdapter: LoginPagerAdapter
    lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewPager = binding.vpLogin
        loginPagerAdapter = LoginPagerAdapter(this)
        viewPager.adapter = loginPagerAdapter

        TabLayoutMediator(binding.tlLogin, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Login"
                1 -> tab.text = "Sign up"
            }
        }.attach()
        supportActionBar?.hide()

        binding.fabChangeRole.setOnClickListener {
            SharedPrefManager.putString(SHARE_PREF_ROLE, ROLE_NAN)
            startActivity(Intent(this, PickRoleActivity::class.java))
        }
    }
}
