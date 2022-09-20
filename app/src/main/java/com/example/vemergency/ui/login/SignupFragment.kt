package com.example.vemergency.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.vemergency.databinding.FragmentSignupBinding

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //signupApi = RetrofitClient.getInstance(requireContext()).getClientUnAuthorize().create(BookService::class.java)
        binding.btnSignUp.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            performSignUp()
        }
    }

    private fun performSignUp() {
//        val username = edtUsername.text.toString()
//        val password = edtPassword.text.toString()
//        val passwordConfirm = edtPasswordConfirm.text.toString()
//        if (password != "" && username !="" &&passwordConfirm != "") {
//            if (password == passwordConfirm) {
//                signupApi.register(createJsonRequestBody(
//                    "username" to username, "password" to password)).enqueue(object : Callback<Account?> {
//                    override fun onResponse(
//                        call: Call<Account?>,
//                        response: Response<Account?>
//                    ) {
//                        if (response.isSuccessful) {
//                            val registerResponse = response.body()
//                            loadingProgressBar.visibility = View.GONE
//                            edtUsername.text.clear()
//                            edtPassword.text.clear()
//                            edtPasswordConfirm.text.clear()
//                            showToast("Register successfully!")
//                            navigateToLoginFragment()
//                            Log.e("onResponse: ", registerResponse.toString())
//                        } else {
//                            loadingProgressBar.visibility = View.GONE
//                            showToast(response.body().toString())
//                        }
//                    }
//
//                    override fun onFailure(call: Call<Account?>, t: Throwable) {
//                        loadingProgressBar.visibility = View.GONE
//                        Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
//                    }
//                })
//            }
//        } else {
//            Toast.makeText(requireContext(), "Please fill all field", Toast.LENGTH_LONG).show()
//        }
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
