package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        try {
            if (auth.currentUser != null) {
                checkUserRoleAndNavigate(auth.currentUser!!.uid)
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error checking current user", e)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnSignIn = findViewById<MaterialButton>(R.id.btnSignIn)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnSignIn.setOnClickListener {
            try {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        try {
                            if (task.isSuccessful) {
                                val uid = auth.currentUser!!.uid
                                checkUserRoleAndNavigate(uid)
                            } else {
                                Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error processing login success", e)
                        }
                    }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Unexpected error during login", e)
                Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun checkUserRoleAndNavigate(uid: String) {
        try {
            val database = FirebaseDatabase.getInstance().getReference("users").child(uid)

            database.get().addOnSuccessListener { snapshot ->
                try {
                    if (snapshot.exists()) {
                        val status = snapshot.child("status").value?.toString() ?: "Active"

                        if (status != "Active") {
                            auth.signOut()
                            Toast.makeText(this, "Your account is $status. Please contact admin.", Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }

                        val role = snapshot.child("role").value?.toString() ?: "User"

                        if (role == "Admin") {
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        finishAffinity()
                    } else {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error parsing user data", e)
                    startActivity(Intent(this, MainActivity::class.java)) // Safe fallback
                    finishAffinity()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Network error fetching profile", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error executing database call", e)
        }
    }
}