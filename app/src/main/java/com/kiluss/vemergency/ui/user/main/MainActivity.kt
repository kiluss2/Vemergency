package com.kiluss.vemergency.ui.user.main

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
import com.kiluss.vemergency.constant.EXTRA_CREATE_SHOP
import com.kiluss.vemergency.constant.LOGIN_FRAGMENT_EXTRA
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.ActivityMainBinding
import com.kiluss.vemergency.ui.user.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private var backPressPreviousState: Boolean = false
    private lateinit var binding: ActivityMainBinding

    // view model ktx
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize the bottom navigation view
        //create bottom navigation view object
        binding.bottomNavigationView.setupWithNavController(findNavController(R.id.navFragment))

        FirebaseManager.init()
        setUpOnClickView()
    }

    private fun setUpOnClickView() {
        binding.fabAddPlace.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                putExtra(
                    LOGIN_FRAGMENT_EXTRA,
                    EXTRA_CREATE_SHOP
                )
            })
        }
    }

    override fun onBackPressed() {
        if (backPressPreviousState) {
            super.onBackPressed()
        } else {
            backPressPreviousState = true
            Toast.makeText(this, "Press one more time to exit", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                backPressPreviousState = false
            }, 3000)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra(EXTRA_CREATED_SHOP) != null) {
            // binding.bottomNavigationView.selectedItemId = R.id.myShopFragment
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserInfo()
    }
}
