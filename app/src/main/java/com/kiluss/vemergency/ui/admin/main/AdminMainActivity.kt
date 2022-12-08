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
import com.kiluss.vemergency.constant.EXTRA_CREATED_SHOP
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private var backPressPreviousState = false
    private lateinit var binding: ActivityAdminMainBinding

    // view model ktx
    private val viewModel: AdminMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize the bottom navigation view
        // create bottom navigation view object
        binding.bottomNavigationView.setupWithNavController(findNavController(R.id.navFragment))

        observeViewModel()
        FirebaseManager.init()
        setUpOnClickView()
        // viewModel.getShopCloneSize()
        // viewModel.bindJSONDataInShopList(this)
    }

    private fun observeViewModel() {
        with(viewModel) {
        }
    }

    private fun setUpOnClickView() {
    }

    override fun onResume() {
        super.onResume()
        viewModel.getActiveShop()
        viewModel.getAllUser()
        viewModel.getAdminInfo()
    }

    override fun onBackPressed() {
        val id = findNavController(R.id.navFragment).currentDestination?.id
        if (id == R.id.manageShopFragment && !backPressPreviousState) {
            backPressPreviousState = true
            Toast.makeText(this, "Press one more time to exit", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                backPressPreviousState = false
            }, 3000)
        } else if (id != R.id.manageShopFragment) {
            super.onBackPressed()
            backPressPreviousState = false
        } else if (backPressPreviousState) {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra(EXTRA_CREATED_SHOP) != null) {
            // binding.bottomNavigationView.selectedItemId = R.id.myShopFragment
        }
    }
}
