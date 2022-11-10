package com.kiluss.vemergency.ui.user.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.databinding.ActivityPickRoleBinding
import com.kiluss.vemergency.ui.login.LoginActivity
import com.kiluss.vemergency.ui.user.main.MainActivity
import com.kiluss.vemergency.utils.SharedPrefManager

class PickRoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickRoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPreviousRole()
        setupView()
    }

    private fun checkPreviousRole() {
        when (SharedPrefManager.getString(SHARE_PREF_ROLE, ROLE_NAN)) {
            ROLE_USER -> {
                startActivity(Intent(this@PickRoleActivity, MainActivity::class.java))
                finish()
            }
            ROLE_SHOP -> {
                startActivity(Intent(this@PickRoleActivity, LoginActivity::class.java))
                finish()
            }
            ROLE_ADMIN -> {
                startActivity(Intent(this@PickRoleActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun setupView() {
        with(binding) {
            btnAdmin.setOnClickListener {
                SharedPrefManager.putString(SHARE_PREF_ROLE, ROLE_ADMIN)
                startActivity(Intent(this@PickRoleActivity, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
                finish()
            }
            btnShop.setOnClickListener {
                SharedPrefManager.putString(SHARE_PREF_ROLE, ROLE_SHOP)
                startActivity(Intent(this@PickRoleActivity, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
                finish()
            }
            btnUser.setOnClickListener {
                SharedPrefManager.putString(SHARE_PREF_ROLE, ROLE_USER)
                startActivity(Intent(this@PickRoleActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
                finish()
            }
        }
    }
}
