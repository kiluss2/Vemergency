package com.kiluss.vemergency.ui.shop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kiluss.vemergency.databinding.ActivityAddNewShopBinding

class AddNewShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewShopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
