package com.example.lostandfound

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        // Static action to simulate registration
        btnCreateAccount.setOnClickListener {
            Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
            finish() // Goes back to Login screen
        }

        // Navigate back to Login
        tvBackToLogin.setOnClickListener {
            finish() // Closes RegisterActivity and shows LoginActivity underneath
        }
    }
}