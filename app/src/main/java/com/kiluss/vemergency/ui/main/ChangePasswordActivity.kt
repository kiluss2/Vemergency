package com.kiluss.vemergency.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.kiluss.vemergency.data.firebase.FirebaseManager
import com.kiluss.vemergency.databinding.ActivityChangePasswordBinding
import com.kiluss.vemergency.utils.Utils


class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Change password"

        binding.btnSubmit.setOnClickListener {
            val credential = EmailAuthProvider
                .getCredential(
                    FirebaseManager.getCurrentUser()?.email.toString(),
                    binding.edtCurrentPassword.text.toString()
                )
            FirebaseManager.getCurrentUser()?.reauthenticate(credential)
                ?.addOnCompleteListener {
                    if (binding.edtNewPassword.text.toString() == binding.edtConfirmNewPassword.text.toString() &&
                        binding.edtNewPassword.text.isNotEmpty() &&
                        binding.edtNewPassword.text.toString() != binding.edtCurrentPassword.text.toString()
                    ) {
                        uploadChange()
                    } else {
                        if (binding.edtNewPassword.text.toString() != binding.edtConfirmNewPassword.text.toString()) {
                            binding.edtConfirmNewPassword.error = "Confirm password not match"
                        }
                        if (binding.edtNewPassword.text.toString() == binding.edtCurrentPassword.text.toString()) {
                            binding.edtNewPassword.error = "Current and new password is the same"
                        }
                    }
                    if (binding.edtCurrentPassword.text.isEmpty()
                    ) {
                        binding.edtCurrentPassword.error = "Please fill in field"
                    }
                    if (binding.edtNewPassword.text.isEmpty()
                    ) {
                        binding.edtNewPassword.error = "Please fill in field"
                    }
                    if (binding.edtConfirmNewPassword.text.isEmpty()
                    ) {
                        binding.edtConfirmNewPassword.error = "Please fill in field"
                    }
                }
                ?.addOnFailureListener {
                    binding.edtCurrentPassword.error = "Wrong current password"
                }
        }
    }

    private fun uploadChange() {
        FirebaseManager.getCurrentUser()?.updatePassword(binding.edtNewPassword.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.showShortToast(this, "Update successfully")
                    finish()
                } else {
                    Utils.showLongToast(this, task.exception.toString())
                }
            }
    }
}
