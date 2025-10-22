package com.example.chatapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chatapplication.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Validate email on focus loss (kept from your version)
        binding.myEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.textInputLayout5.error = null
            } else {
                val emailOk = Patterns.EMAIL_ADDRESS
                    .matcher(binding.myEmail.text.toString().trim())
                    .matches()
                binding.textInputLayout5.error = if (emailOk) null else "Invalid Email"
            }
        }

        // Handle IME action (Go / Done / Enter)
        binding.myPassword.setOnEditorActionListener { v, actionId, event ->
            val isSubmitAction =
                actionId == EditorInfo.IME_ACTION_GO ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_UNSPECIFIED ||
                        (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)

            if (isSubmitAction) {
                hideKeyboardAndClearFocus()
                loginAcc()
                true
            } else {
                false
            }
        }

        binding.loginAccBtn.setOnClickListener {
            hideKeyboardAndClearFocus()
            loginAcc()
        }

        binding.createAccActivityBtn.setOnClickListener {
            switchToCreateAcc()
        }
    }

    private fun hideKeyboardAndClearFocus() {
        val root = binding.root

        // Move focus off any EditText, so IMM can close IME
        root.isFocusableInTouchMode = true
        root.requestFocus()

        // Try WindowInsets (modern API)
        try {
            WindowInsetsControllerCompat(window, root)
                .hide(WindowInsetsCompat.Type.ime())
        } catch (_: Throwable) { /* no-op */ }

        // Fallback to IMM
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(root.windowToken, 0)
    }

    private fun switchToCreateAcc() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private fun loginAcc() {
        val email = binding.myEmail.text?.toString()?.trim().orEmpty()
        val password = binding.myPassword.text?.toString().orEmpty()

        // Validate here as well so we don't depend on focus changes
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayout5.error = "Invalid Email"
            Toast.makeText(this, "Invalid Email Entered", Toast.LENGTH_SHORT).show()
            return
        }

        setProgressBar(true)
        FirebaseRefs.auth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setProgressBar(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    binding.myPassword.text?.clear()
                    Toast.makeText(this, "Incorrect Credentials", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setProgressBar(progress: Boolean) {
        binding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }
}
