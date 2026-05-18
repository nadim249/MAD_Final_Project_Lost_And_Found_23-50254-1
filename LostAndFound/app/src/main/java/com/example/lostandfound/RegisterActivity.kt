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

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnCreateAccount.setOnClickListener {
            try {
                val name = etFullName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val confirmPass = etConfirmPassword.text.toString().trim()

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password != confirmPass) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create User in Firebase Auth
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        try {
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                val uid = firebaseUser?.uid

                                val initials = name.split(' ').mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()

                                val userMap = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "role" to "User",
                                    "department" to "Not specified",
                                    "phone" to "Not specified",
                                    "campus" to "Not specified",
                                    "initials" to initials,
                                    "status" to "Active",
                                    "stats" to hashMapOf(
                                        "itemsLost" to 0,
                                        "itemsFound" to 0,
                                        "resolved" to 0
                                    )
                                )

                                if (uid != null) {
                                    database.getReference("users").child(uid).setValue(userMap)
                                        .addOnCompleteListener { dbTask ->
                                            if (dbTask.isSuccessful) {
                                                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finishAffinity()
                                            } else {
                                                Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Auth Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("RegisterActivity", "Error processing successful auth", e)
                            Toast.makeText(this, "An error occurred setting up your profile.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Unexpected error during registration", e)
                Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        tvBackToLogin.setOnClickListener { finish() }
    }
}