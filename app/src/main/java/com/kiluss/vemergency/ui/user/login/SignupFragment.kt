package com.kiluss.vemergency.ui.user.login

import android.content.ContentValues
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
import com.kiluss.vemergency.constant.*
import com.kiluss.vemergency.databinding.FragmentSignupBinding
import com.kiluss.vemergency.utils.SharedPrefManager
import com.kiluss.vemergency.utils.Utils

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth
        //signupApi = RetrofitClient.getInstance(requireContext()).getClientUnAuthorize().create(BookService::class.java)
        binding.btnSignUp.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            performSignUp()
        }
    }

    private fun performSignUp() {
        with(binding) {
            val username = edtUsername.text.toString()
            val password = edtPassword.text.toString()
            val passwordConfirm = edtPasswordConfirm.text.toString()
            if (password != "" && username != "" && passwordConfirm != "") {
                if (password == passwordConfirm) {
                    auth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                binding.loading.visibility = View.GONE
                                auth.uid?.let {
                                    db.collection(Utils.getCollectionRole())
                                        .document(it)
                                        .set(HashMap<String, Any>())
                                        .addOnSuccessListener {
                                            // Sign in success, update UI with the signed-in user's information
                                            Toast.makeText(
                                                activity, "Success!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            auth.signOut()
                                            navigateToLoginFragment()
                                        }
                                        .addOnFailureListener { e ->
                                            auth.signOut()
                                            Utils.showShortToast(
                                                this@SignupFragment.requireContext(),
                                                "Fail to update profile"
                                            )
                                            Log.e(ContentValues.TAG, "Error adding document", e)
                                        }
                                }
                            } else {
                                auth.signOut()
                                binding.loading.visibility = View.GONE
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.exception)
                                Toast.makeText(activity, task.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                auth.signOut()
                Toast.makeText(requireContext(), "Please fill all field", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToLoginFragment() {
        (activity as LoginActivity).viewPager.currentItem = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
