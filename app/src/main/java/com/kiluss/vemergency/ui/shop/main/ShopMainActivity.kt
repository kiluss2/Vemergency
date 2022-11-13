package com.kiluss.vemergency.ui.shop.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_PENDING_TRANSACTION_FRAGMENT
import com.kiluss.vemergency.constant.EXTRA_TRANSACTION
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.ActivityShopMainBinding
import com.kiluss.vemergency.ui.shop.rescue.ShopRescueActivity

class ShopMainActivity : AppCompatActivity() {
    private var backPressPreviousState = false
    private lateinit var binding: ActivityShopMainBinding

    // view model ktx
    private val viewModel: ShopMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopMainBinding.inflate(layoutInflater)
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
            navigateToHome.observe(this@ShopMainActivity) {
                binding.bottomNavigationView.selectedItemId = R.id.myShopFragment
            }
            startRescue.observe(this@ShopMainActivity) {
                startActivity(Intent(this@ShopMainActivity, ShopRescueActivity::class.java).apply {
                    putExtra(EXTRA_TRANSACTION, it)
                })
            }
        }
    }

    private fun setUpOnClickView() {
    }

    override fun onBackPressed() {
        val id = findNavController(R.id.navFragment).currentDestination?.id
        if (id == R.id.myShopFragment && !backPressPreviousState) {
            backPressPreviousState = true
            Toast.makeText(this, "Press one more time to exit", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                backPressPreviousState = false
            }, 3000)
        } else if (id != R.id.myShopFragment) {
            super.onBackPressed()
            backPressPreviousState = false
        } else if (backPressPreviousState) {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra(EXTRA_PENDING_TRANSACTION_FRAGMENT) != null) {
            binding.bottomNavigationView.selectedItemId = R.id.pendingTransactionFragment
        }
    }
}
