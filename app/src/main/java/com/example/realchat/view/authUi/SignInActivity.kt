package com.example.realchat.view.authUi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.realchat.databinding.ActivitySignInBinding
import com.example.realchat.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.toolbar.title = "log In Registration"

        binding.signUpButton.setOnClickListener {
            signUp()
        }


        binding.logInButton.setOnClickListener {
            signIn()
        }

    }

    public override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            gotoMainActivity()

        } else {
            Toast.makeText(this, "Please Log in First", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gotoMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun signUp() {
        if (binding.emailEditText.text.isNotEmpty() && binding.passwordEdiText.text.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(
                binding.emailEditText.text.toString().trim(),
                binding.passwordEdiText.text.toString().trim()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "check this field", Toast.LENGTH_SHORT).show()
        }

    }

    private fun signIn() {
        if (binding.emailEditText.text.isNotEmpty() && binding.passwordEdiText.text.isNotEmpty()) {
            auth.signInWithEmailAndPassword(
                binding.emailEditText.text.toString().trim(),
                binding.passwordEdiText.text.toString().trim()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        gotoMainActivity()

                    } else {
                        Toast.makeText(
                            this, "log in Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        } else {
            Toast.makeText(this, "check this field", Toast.LENGTH_SHORT).show()
        }
    }


}