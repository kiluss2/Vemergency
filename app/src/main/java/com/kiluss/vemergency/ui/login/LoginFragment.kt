package com.kiluss.vemergency.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.R
import com.kiluss.vemergency.constant.EXTRA_CHANGE_PASSWORD
import com.kiluss.vemergency.constant.EXTRA_EDIT_USER_PROFILE
import com.kiluss.vemergency.constant.EXTRA_EMERGENCY
import com.kiluss.vemergency.constant.EXTRA_USER_PROFILE
import com.kiluss.vemergency.constant.FCM_DEVICE_TOKEN
import com.kiluss.vemergency.constant.LOGIN_FRAGMENT_EXTRA
import com.kiluss.vemergency.constant.ROLE_ADMIN
import com.kiluss.vemergency.constant.ROLE_NAN
import com.kiluss.vemergency.constant.ROLE_SHOP
import com.kiluss.vemergency.constant.ROLE_USER
import com.kiluss.vemergency.constant.SHARE_PREF_ROLE
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.FragmentLoginBinding
import com.kiluss.vemergency.ui.admin.main.AdminMainActivity
import com.kiluss.vemergency.ui.shop.main.ShopMainActivity
import com.kiluss.vemergency.ui.user.emergency.CreateEmergencyActivity
import com.kiluss.vemergency.ui.user.main.ChangePasswordActivity
import com.kiluss.vemergency.ui.user.userprofile.EditUserProfileActivity
import com.kiluss.vemergency.ui.user.userprofile.UserProfileActivity
import com.kiluss.vemergency.utils.SharedPrefManager
import com.kiluss.vemergency.utils.Utils

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Initialize Firebase Auth
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loginSuccess()
        }

        binding.btnSignIn.setOnClickListener {
            binding.pbLoading.visibility = View.VISIBLE
            performCheckLogin()
        }
    }

    private fun performCheckLogin() {
        with(binding) {
            val username = username.text.toString()
            val password = password.text.toString()
            if (username != "" && password != "") {
                auth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseManager.init()
                            auth.currentUser?.uid?.let { uid ->
                                val collectionRole = Utils.getCollectionRole()
                                // check if this account is a right role
                                db.collection(collectionRole).document(uid).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        binding.pbLoading.visibility = View.GONE
                                        if (documentSnapshot.exists()) {
                                            if (collectionRole == ROLE_SHOP) {
                                                db.collection(collectionRole).document(uid).update(
                                                    "fcmToken", SharedPrefManager.getString(
                                                        FCM_DEVICE_TOKEN, ""
                                                    ),
                                                    "ready", true
                                                )
                                            }
                                            loginSuccess()
                                        } else {
                                            Utils.showLongToast(
                                                this@LoginFragment.requireContext(),
                                                getString(R.string.this_account_is_not_belong_to_this_role)
                                            )
                                            auth.signOut()
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        binding.pbLoading.visibility = View.GONE
                                        Utils.showLongToast(
                                            this@LoginFragment.requireContext(),
                                            exception.message.toString()
                                        )
                                        auth.signOut()
                                    }
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.exception)
                            Toast.makeText(activity, task.exception?.message, Toast.LENGTH_SHORT).show()
                            binding.pbLoading.visibility = View.GONE
                        }
                    }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please make sure to fill in your email and password",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loginSuccess() {
        when (SharedPrefManager.getString(SHARE_PREF_ROLE, ROLE_NAN)) {
            ROLE_USER -> {
                when (activity?.intent?.getStringExtra(LOGIN_FRAGMENT_EXTRA)) {
                    EXTRA_USER_PROFILE -> {
                        requireActivity().startActivity(
                            Intent(requireActivity(), UserProfileActivity::class.java)
                        )
                    }
                    EXTRA_EDIT_USER_PROFILE -> {
                        requireActivity().startActivity(
                            Intent(requireActivity(), EditUserProfileActivity::class.java)
                        )
                    }
                    EXTRA_CHANGE_PASSWORD -> {
                        requireActivity().startActivity(
                            Intent(requireActivity(), ChangePasswordActivity::class.java)
                        )
                    }
                    EXTRA_EMERGENCY -> {
                        requireActivity().startActivity(
                            Intent(requireActivity(), CreateEmergencyActivity::class.java)
                        )
                    }
                }
            }
            ROLE_SHOP -> {
                startActivity(Intent(this@LoginFragment.activity, ShopMainActivity::class.java))
            }
            ROLE_ADMIN -> {
                startActivity(Intent(this@LoginFragment.activity, AdminMainActivity::class.java))
            }
        }
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
