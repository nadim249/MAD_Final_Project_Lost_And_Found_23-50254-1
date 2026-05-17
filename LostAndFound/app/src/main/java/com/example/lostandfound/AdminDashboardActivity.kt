package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Stats cards
        findViewById<View>(R.id.cardTotalUsers).setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
        findViewById<View>(R.id.cardActiveItems).setOnClickListener {
            startActivity(Intent(this, ItemsListActivity::class.java))
        }
        findViewById<View>(R.id.cardResolved).setOnClickListener {
           startActivity(Intent(this, ItemsListActivity::class.java))
        }
        findViewById<View>(R.id.cardPendingReports).setOnClickListener {
            // startActivity(Intent(this, ReportsActivity::class.java))
        }

        // Quick action cards
        findViewById<View>(R.id.cardManageUsers).setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
        findViewById<View>(R.id.cardViewItems).setOnClickListener {
            startActivity(Intent(this, ItemsListActivity::class.java))
        }
        findViewById<View>(R.id.cardClaimRequests).setOnClickListener {
            startActivity(Intent(this, ClaimRequestsActivity::class.java))
        }
        findViewById<View>(R.id.cardReportsAction).setOnClickListener {
            // startActivity(Intent(this, ReportsActivity::class.java))
        }

        findViewById<View>(R.id.logoutbtn).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Admin Sign Out")
                .setMessage("Are you sure you want to close the control panel and sign out?")
                .setPositiveButton("Sign Out") { _, _ ->

                    // 1. Sign out of Firebase
                    FirebaseAuth.getInstance().signOut()

                    // 2. Route back to Login screen safely
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


    }
}