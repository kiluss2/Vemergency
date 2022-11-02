package com.kiluss.vemergency.ui.user.emergency

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kiluss.vemergency.databinding.ActivityCreateEmergencyBinding

class CreateEmergencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEmergencyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
