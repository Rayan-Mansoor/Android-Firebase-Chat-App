package com.example.chatapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chatapplication.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Optional: keep your focus listeners just to show inline errors early ---
        binding.regEmail.setOnFocusChangeListener { _, hasFocus ->
            Log.d("CreateAccount", "Email focus changed: $hasFocus")
            if (hasFocus) {
                binding.textInputLayout3.error = null
            } else {
                val emailOk = Patterns.EMAIL_ADDRESS
                    .matcher(binding.regEmail.text.toString().trim())
                    .matches()
                binding.textInputLayout3.error = if (emailOk) null else "Invalid Email"
            }
        }

        binding.regPassword.setOnFocusChangeListener { _, hasFocus ->
            Log.d("CreateAccount", "Password focus changed: $hasFocus")
            if (hasFocus) {
                binding.textInputLayout.error = null
            } else {
                val pw = binding.regPassword.text?.toString().orEmpty()
                binding.textInputLayout.error =
                    if (pw.length < 6) "Password length should be at least 6" else null
            }
        }

        binding.regRetypePassword.setOnFocusChangeListener { _, hasFocus ->
            Log.d("CreateAccount", "Confirm password focus changed: $hasFocus")
            if (hasFocus) {
                binding.textInputLayout2.error = null
            } else {
                val pw = binding.regPassword.text?.toString().orEmpty()
                val cpw = binding.regRetypePassword.text?.toString().orEmpty()
                binding.textInputLayout2.error =
                    when {
                        cpw.length < 6 -> "Password length should be at least 6"
                        cpw != pw -> "Passwords don't match"
                        else -> null
                    }
            }
        }

        // IME action on the confirm-password field
        binding.regRetypePassword.setOnEditorActionListener { _, actionId, event ->
            val isSubmitAction =
                actionId == EditorInfo.IME_ACTION_GO ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_UNSPECIFIED ||
                        (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)

            if (isSubmitAction) {
                hideKeyboardAndClearFocus()
                createAcc()
                true
            } else false
        }

        binding.regAccBtn.setOnClickListener {
            hideKeyboardAndClearFocus()
            createAcc()
        }

        binding.loginAccActivityBtn.setOnClickListener { switchToLoginAcc() }
    }

    private fun hideKeyboardAndClearFocus() {
        val root = binding.root

        // Move focus off fields so IME can close
        root.isFocusableInTouchMode = true
        root.requestFocus()

        // Modern insets API
        try {
            WindowInsetsControllerCompat(window, root)
                .hide(WindowInsetsCompat.Type.ime())
        } catch (_: Throwable) { /* no-op */ }

        // IMM fallback
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(root.windowToken, 0)
    }

    private fun switchToLoginAcc() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setProgressBar(progress: Boolean) {
        binding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }

    private fun createAcc() {
        val email = binding.regEmail.text?.toString()?.trim().orEmpty()
        val pw = binding.regPassword.text?.toString().orEmpty()
        val cpw = binding.regRetypePassword.text?.toString().orEmpty()

        // Final validation independent of focus state
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayout3.error = "Invalid Email"
            Toast.makeText(this, "Invalid data entered", Toast.LENGTH_SHORT).show()
            return
        }
        if (pw.length < 6) {
            binding.textInputLayout.error = "Password length should be at least 6"
            Toast.makeText(this, "Invalid data entered", Toast.LENGTH_SHORT).show()
            return
        }
        if (cpw != pw) {
            binding.textInputLayout2.error = "Passwords don't match"
            Toast.makeText(this, "Invalid data entered", Toast.LENGTH_SHORT).show()
            return
        }

        setProgressBar(true)
        FirebaseRefs.auth.createUserWithEmailAndPassword(email, pw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Account Created Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val createdUser = User(FirebaseRefs.uid!!, email)
                    FirebaseRefs.users.document(createdUser.userID).set(createdUser)
                        .addOnCompleteListener {
                            setProgressBar(false)
                            FirebaseRefs.auth.signOut()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                } else {
                    setProgressBar(false)
                    Toast.makeText(
                        this,
                        "Account Creation Failed. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}