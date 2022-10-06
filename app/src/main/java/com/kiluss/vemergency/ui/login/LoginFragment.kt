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
import com.google.firebase.ktx.Firebase
import com.kiluss.vemergency.constant.EXTRA_CHANGE_PASSWORD
import com.kiluss.vemergency.constant.EXTRA_CREATE_SHOP
import com.kiluss.vemergency.constant.EXTRA_EDIT_USER_PROFILE
import com.kiluss.vemergency.constant.EXTRA_USER_PROFILE
import com.kiluss.vemergency.constant.LOGIN_FRAGMENT_EXTRA
import com.kiluss.vemergency.constant.SAVED_LOGIN_ACCOUNT_KEY
import com.kiluss.vemergency.constant.SIGN_IN_KEY
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.FragmentLoginBinding
import com.kiluss.vemergency.ui.main.ChangePasswordActivity
import com.kiluss.vemergency.ui.shop.AddNewShopActivity
import com.kiluss.vemergency.ui.userprofile.EditUserProfileActivity
import com.kiluss.vemergency.ui.userprofile.UserProfileActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val sharedPref = requireActivity().getSharedPreferences(SAVED_LOGIN_ACCOUNT_KEY, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(SIGN_IN_KEY, false)) {
            loginSuccess()
        }
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
                            loginSuccess()
                            binding.pbLoading.visibility = View.GONE
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
        when (activity?.intent?.getStringExtra(LOGIN_FRAGMENT_EXTRA)) {
            EXTRA_USER_PROFILE -> {
                requireActivity().startActivity(
                    Intent(requireActivity(), UserProfileActivity::class.java)
                )
                requireActivity().finish()
            }
            EXTRA_CREATE_SHOP -> {
                requireActivity().startActivity(
                    Intent(requireActivity(), AddNewShopActivity::class.java)
                )
                requireActivity().finish()
            }
            EXTRA_EDIT_USER_PROFILE -> {
                requireActivity().startActivity(
                    Intent(requireActivity(), EditUserProfileActivity::class.java)
                )
                requireActivity().finish()
            }
            EXTRA_CHANGE_PASSWORD -> {
                requireActivity().startActivity(
                    Intent(requireActivity(), ChangePasswordActivity::class.java)
                )
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
