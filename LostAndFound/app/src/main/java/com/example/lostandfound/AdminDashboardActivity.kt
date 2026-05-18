package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvActiveItems: TextView
    private lateinit var tvResolved: TextView
    private lateinit var tvPendingReports: TextView
    private lateinit var tvViewItemsCount: TextView
    private lateinit var tvClaimCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance()

        // Initialize TextViews
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvActiveItems = findViewById(R.id.tvActiveItems)
        tvResolved = findViewById(R.id.tvResolved)
        tvPendingReports = findViewById(R.id.tvPendingReports)
        tvViewItemsCount = findViewById(R.id.tvViewItemsCount)
        tvClaimCount = findViewById(R.id.tvClaimCount)

        // Fetch Data
        fetchStats()

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
            startActivity(Intent(this, ReportsActivity::class.java))
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
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        findViewById<View>(R.id.logoutbtn).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Admin Sign Out")
                .setMessage("Are you sure you want to close the control panel and sign out?")
                .setPositiveButton("Sign Out") { _, _ ->

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun fetchStats() {
        // Fetch Users Count
        database.getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount
                tvTotalUsers.text = count.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Items Stats
        database.getReference("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var activeCount = 0
                var resolvedCount = 0
                for (itemSnap in snapshot.children) {
                    val status = itemSnap.child("status").getValue(String::class.java)
                    if (status == "Active") {
                        activeCount++
                    } else if (status == "Resolved" || status == "Found") { // Checking for common resolved statuses
                        resolvedCount++
                    }
                }
                tvActiveItems.text = activeCount.toString()
                tvViewItemsCount.text = activeCount.toString()
                tvResolved.text = resolvedCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Claims Count
        database.getReference("claims").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var pendingClaims = 0
                for (claimSnap in snapshot.children) {
                    val status = claimSnap.child("status").getValue(String::class.java)
                    if (status == "Pending") {
                        pendingClaims++
                    }
                }
                tvClaimCount.text = pendingClaims.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Reports Count
        database.getReference("reports").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var pendingCount = 0
                for (reportSnap in snapshot.children) {
                    val status = reportSnap.child("status").getValue(String::class.java)
                    if (status == "Pending") {
                        pendingCount++
                    }
                }
                tvPendingReports.text = pendingCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                tvPendingReports.text = "0"
            }
        })
    }
}
