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
import com.kiluss.vemergency.databinding.FragmentLoginBinding
import com.kiluss.vemergency.ui.constant.SAVED_LOGIN_ACCOUNT_KEY
import com.kiluss.vemergency.ui.constant.SIGN_IN_KEY
import com.kiluss.vemergency.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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

    private fun loginSuccess() {
        requireActivity().startActivity(
            Intent(
                requireActivity(),
                MainActivity::class.java
            )
        )
        requireActivity().finish()
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
            binding.pbLoading.visibility = View.GONE
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
                            Log.d("TAG", "signInWithEmail:success")
                            loginSuccess()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.exception)
                            Toast.makeText(activity, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Please fill all field", Toast.LENGTH_LONG).show()
            }
        }
    }

//    private fun saveLoginInfo(loginObject: LoginResponse) {
//        val pref: SharedPreferences =
//            requireContext().getSharedPreferences(
//                requireContext().getString(
//                    R.string.saved_login_account_key
//                ),
//                Context.MODE_PRIVATE
//            )
//        val editor: SharedPreferences.Editor = pref.edit()
//        val gson = Gson()
//        val json = gson.toJson(loginObject)
//        editor.putString(
//            requireContext().getString(R.string.saved_login_account_key),
//            json
//        ).apply()
//        editor.putBoolean(
//            requireContext().getString(R.string.is_sign_in_key),
//            true
//        ).apply()
//    }
//
//    private fun createJsonRequestBody(vararg params: Pair<String, Any>) =
//        RequestBody.create(
//            okhttp3.MediaType.parse("application/json; charset=utf-8"),
//            JSONObject(mapOf(*params)).toString()
//        )
//
//    private fun showToastError(response: Response<String?>) {
//        val responseJsonString = response.errorBody()?.charStream()?.readText().toString()
//        if (responseJsonString.isNotBlank()) {
//            if (responseJsonString.contains("errors")) {
//                val errorString = JSONObject(responseJsonString).getString("errors")
//                var passwordErrorString = ""
//                var usernameErrorString = ""
//                if (errorString.contains("Password")) {
//                    passwordErrorString = JSONObject(errorString).getString("Password")
//                    passwordErrorString = passwordErrorString.replace("[\"", "").replace("\"]", "")
//                }
//                if (errorString.contains("UserName")) {
//                    usernameErrorString = JSONObject(errorString).getString("UserName")
//                    usernameErrorString = usernameErrorString.replace("[\"", "").replace("\"]", "")
//                }
//                showLoginFailed("$passwordErrorString $usernameErrorString")
//            } else {
//                showLoginFailed(JSONObject(responseJsonString).getString("message"))
//            }
//        }
//    }

    private fun showLoginFailed(errorString: String) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
