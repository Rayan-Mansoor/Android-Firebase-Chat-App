package com.example.chatapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.chatapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private var isInfoValid : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myEmail.hint = "Email Address"
        binding.myPassword.hint = "Password"

        binding.myEmail.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus){
                binding.textInputLayout5.error = null
            }
            else if (!hasFocus){
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.myEmail.text.toString()).matches()){
                    isInfoValid = false
                    binding.textInputLayout5.error = "Invalid Email"
                }
                else{
                    isInfoValid = true
                }
            }
        }

        binding.myEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.myEmail.hint = null
                if (binding.myEmail.text.isNullOrEmpty()){
                    binding.myEmail.hint = "Email Address"
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.myPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.myPassword.hint = null
                if (binding.myPassword.text.isNullOrEmpty()){
                    binding.myPassword.hint = "Password"
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })


        binding.loginAccBtn.setOnClickListener {
            binding.myEmail.clearFocus()
            loginAcc()
        }

        binding.createAccActivityBtn.setOnClickListener {
            switchToCreateAcc()
        }

    }

    private fun switchToCreateAcc() {
        startActivity(Intent(this,SignUpActivity::class.java))
        finish()
    }

    private fun loginAcc() {
        if (isInfoValid){
            setProgressBar(true)
            val firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth.signInWithEmailAndPassword(binding.myEmail.text.toString(),binding.myPassword.text.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    setProgressBar(false)
                    Toast.makeText(this,"Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,MainActivity::class.java))
                    finish()
                }
                else{
                    setProgressBar(false)
                    binding.myPassword.text?.clear()
                    Toast.makeText(this,"Incorrect Credentials ", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Toast.makeText(this,"Invalid Email Entered", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setProgressBar(progress : Boolean){
        if (progress){
            binding.progressBar.visibility = View.VISIBLE
        }
        else
            binding.progressBar.visibility = View.INVISIBLE
    }

}