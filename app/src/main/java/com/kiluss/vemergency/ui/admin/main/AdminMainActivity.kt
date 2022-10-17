package com.kiluss.vemergency.ui.admin.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.DELAY_BACK_TO_EXIT_TIME
import com.kiluss.vemergency.constant.EXTRA_CREATED_SHOP
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.ActivityAdminMainBinding
import com.kiluss.vemergency.ui.shop.main.ShopMainViewModel

class AdminMainActivity : AppCompatActivity() {

    private var backPressPreviousState: Boolean = false
    private lateinit var binding: ActivityAdminMainBinding

    // view model ktx
    private val viewModel: ShopMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Initialize the bottom navigation view
        //create bottom navigation view object
        binding.bottomNavigationView.setupWithNavController(findNavController(R.id.navFragment))

        observeViewModel()
        FirebaseManager.init()
        setUpOnClickView()
    }

    private fun observeViewModel() {
        with(viewModel) {
            navigateToHome.observe(this@AdminMainActivity) {
                binding.bottomNavigationView.selectedItemId = R.id.myShopFragment
            }
        }
    }

    private fun setUpOnClickView() {
    }

    override fun onBackPressed() {
        if (backPressPreviousState) {
            super.onBackPressed()
        } else {
            backPressPreviousState = true
            Toast.makeText(this, getString(R.string.press_one_more_time_to_exit), Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                backPressPreviousState = false
            }, DELAY_BACK_TO_EXIT_TIME)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra(EXTRA_CREATED_SHOP) != null) {
            // binding.bottomNavigationView.selectedItemId = R.id.myShopFragment
        }
    }
}
