package com.example.lostandfound

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnSendReset = findViewById<MaterialButton>(R.id.btnSendReset)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        // Static action to simulate sending email
        btnSendReset.setOnClickListener {
            Toast.makeText(this, "Reset link sent to your email!", Toast.LENGTH_LONG).show()
            finish() // Optional: Go back to login after sending
        }

        // Navigate back to Login
        tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}