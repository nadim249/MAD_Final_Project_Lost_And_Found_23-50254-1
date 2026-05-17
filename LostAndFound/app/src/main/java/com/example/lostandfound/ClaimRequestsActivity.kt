package com.example.lostandfound

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



class ClaimRequestsActivity : AppCompatActivity() {


    private lateinit var rvClaims: RecyclerView
    private lateinit var ivBack: View
    private lateinit var tvPendingCount: TextView
    private lateinit var adapter: ClaimRequestAdapter

    private val claims = mutableListOf(
        ClaimRequest("Black Nike Backpack", "John Davis",  "Has a blue keychain and MacBook Pro inside with sticker on lid...", "2 hours ago", "Pending"),
        ClaimRequest("iPhone 13 Pro",       "Lisa Park",   "Blue case, cracked bottom corner, lockscreen is a cat photo...",   "5 hours ago", "Pending"),
        ClaimRequest("Student ID Card",     "Tom Brown",   "My name and photo on front, student ID 20213456...",               "1 day ago",   "Approved"),
        ClaimRequest("AirPods Pro",         "Anna Lee",    "Serial number on case, personalized engraving on lid...",          "2 days ago",  "Rejected"),
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_claim_requests)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ivBack          = findViewById(R.id.ivBack)
        rvClaims        = findViewById(R.id.rvClaimRequests)
        tvPendingCount  = findViewById(R.id.tvPendingCount)

        updatePendingBadge()

        adapter = ClaimRequestAdapter(
            items = claims,
            onApprove = { claim, position ->
                adapter.updateStatus(position, "Approved")
                updatePendingBadge()
                Toast.makeText(this, "✓ Approved: ${claim.itemTitle}", Toast.LENGTH_SHORT).show()
            },
            onReject = { claim, position ->
                adapter.updateStatus(position, "Rejected")
                updatePendingBadge()
                Toast.makeText(this, "✗ Rejected: ${claim.itemTitle}", Toast.LENGTH_SHORT).show()
            }
        )

        rvClaims.layoutManager = LinearLayoutManager(this)
        rvClaims.adapter = adapter

        ivBack.setOnClickListener { finish() }
    }

    private fun updatePendingBadge() {
        val pendingCount = claims.count { it.status == "Pending" }
        tvPendingCount.text = "$pendingCount Pending"
    }
}