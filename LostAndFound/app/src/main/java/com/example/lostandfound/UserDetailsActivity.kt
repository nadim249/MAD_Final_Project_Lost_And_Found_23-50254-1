package com.example.lostandfound

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

import com.google.firebase.database.FirebaseDatabase

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var user: UserModel
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_details)
        
        database = FirebaseDatabase.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        @Suppress("DEPRECATION")
        user = intent.getSerializableExtra("USER_DATA") as UserModel

        setupUI()
    }

    private fun setupUI() {
        val tvInitials = findViewById<TextView>(R.id.tvDetailInitials)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvEmail = findViewById<TextView>(R.id.tvDetailEmail)
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val spinnerStatus = findViewById<AutoCompleteTextView>(R.id.spinnerStatus)
        val tvStats = findViewById<TextView>(R.id.tvDetailStats)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val ivBack = findViewById<ImageView>(R.id.ivBack)

        // Populate data
        tvInitials.text = user.initials
        tvName.text = user.name
        tvEmail.text = user.email
        etName.setText(user.name)
        etEmail.setText(user.email)
        tvStats.text = "User Stats: ${user.stats}"

        // Status Dropdown
        val statuses = arrayOf("Active", "Suspended", "Pending")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        spinnerStatus.setAdapter(adapter)
        spinnerStatus.setText(user.status, false)

        ivBack.setOnClickListener { finish() }

        btnUpdate.setOnClickListener {
            val updatedName = etName.text.toString().trim()
            val updatedEmail = etEmail.text.toString().trim()
            val updatedStatus = spinnerStatus.text.toString()

            if (updatedName.isEmpty() || updatedEmail.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "name" to updatedName,
                "email" to updatedEmail,
                "status" to updatedStatus,
                "initials" to updatedName.split(' ').mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
            )

            database.getReference("users").child(user.uid).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete ${user.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    database.getReference("users").child(user.uid).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to delete user: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
