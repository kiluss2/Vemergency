package com.kiluss.vemergency.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kiluss.vemergency.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

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
            if (password != "" && username !="" &&passwordConfirm != "") {
                if (password == passwordConfirm) {
                    auth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(activity, "Success!",
                                    Toast.LENGTH_SHORT).show()
                                navigateToLoginFragment()
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.exception)
                                Toast.makeText(activity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all field", Toast.LENGTH_LONG).show()
            }
        }
    }

//    private fun createJsonRequestBody(vararg params : Pair<String, Any>) =
//        RequestBody.create(
//            okhttp3.MediaType.parse("application/json; charset=utf-8"),
//            JSONObject(mapOf(*params)).toString())

    private fun navigateToLoginFragment() {
        (activity as LoginActivity).viewPager.currentItem = 0
    }

    private fun showToast(string: String) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, string, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
