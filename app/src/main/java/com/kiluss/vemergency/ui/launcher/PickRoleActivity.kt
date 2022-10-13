package com.kiluss.vemergency.ui.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kiluss.vemergency.databinding.ActivityPickRoleBinding

class PickRoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickRoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
