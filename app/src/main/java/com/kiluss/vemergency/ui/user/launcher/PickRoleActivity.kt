package com.kiluss.vemergency.ui.user.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kiluss.vemergency.constant.ROLE_NAN
import com.kiluss.vemergency.constant.ROLE_USER
import com.kiluss.vemergency.constant.SHARE_PREF_ROLE
import com.kiluss.vemergency.databinding.ActivityPickRoleBinding
import com.kiluss.vemergency.ui.user.main.MainActivity
import com.kiluss.vemergency.utils.SharedPrefManager

class PickRoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickRoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SharedPrefManager.init(applicationContext)
        checkPreviousRole()
        setupView()
    }

    private fun checkPreviousRole() {
        when(SharedPrefManager.getString(SHARE_PREF_ROLE, ROLE_NAN)) {
            ROLE_USER -> {
                startActivity(Intent(this@PickRoleActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setupView() {
        with(binding) {
            btnAdmin.setOnClickListener {  }
            btnShop.setOnClickListener {  }
            btnUser.setOnClickListener {
                SharedPrefManager.putString(SHARE_PREF_ROLE, ROLE_USER)
                startActivity(Intent(this@PickRoleActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
