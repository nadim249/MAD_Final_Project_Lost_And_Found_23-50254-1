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
import com.google.firebase.database.*

class ClaimRequestsActivity : AppCompatActivity() {

    private lateinit var rvClaims: RecyclerView
    private lateinit var ivBack: View
    private lateinit var tvPendingCount: TextView
    private lateinit var adapter: ClaimRequestAdapter
    private lateinit var database: FirebaseDatabase
    
    private val claimsList = mutableListOf<ClaimModel>()
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_claim_requests)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        database = FirebaseDatabase.getInstance()
        ivBack          = findViewById(R.id.ivBack)
        rvClaims        = findViewById(R.id.rvClaimRequests)
        tvPendingCount  = findViewById(R.id.tvPendingCount)

        setupRecyclerView()
        setupFilters()
        fetchClaims()

        ivBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ClaimRequestAdapter(
            items = claimsList,
            onApprove = { claim -> updateClaimStatus(claim, "Approved") },
            onReject = { claim -> updateClaimStatus(claim, "Rejected") }
        )
        rvClaims.layoutManager = LinearLayoutManager(this)
        rvClaims.adapter = adapter
    }

    private fun setupFilters() {
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterPending = findViewById<TextView>(R.id.filterPending)
        val filterApproved = findViewById<TextView>(R.id.filterApproved)
        val filterRejected = findViewById<TextView>(R.id.filterRejected)

        val filters = listOf(filterAll, filterPending, filterApproved, filterRejected)

        filters.forEach { filterView ->
            filterView.setOnClickListener {
                currentFilter = filterView.text.toString()
                updateFilterUI(filters, filterView)
                applyFilter()
            }
        }
    }

    private fun updateFilterUI(filters: List<TextView>, activeFilter: TextView) {
        filters.forEach {
            it.setBackgroundResource(R.drawable.pill_inactive_bg)
            it.setTextColor(android.graphics.Color.parseColor("#888888"))
        }
        activeFilter.setBackgroundResource(R.drawable.pill_active_bg)
        activeFilter.setTextColor(android.graphics.Color.WHITE)
    }

    private fun fetchClaims() {
        database.getReference("claims").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                claimsList.clear()
                var pendingCount = 0
                for (claimSnap in snapshot.children) {
                    val claim = claimSnap.getValue(ClaimModel::class.java)
                    if (claim != null) {
                        claimsList.add(claim)
                        if (claim.status == "Pending") pendingCount++
                    }
                }
                tvPendingCount.text = "$pendingCount Pending"
                applyFilter()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun applyFilter() {
        val filteredList = if (currentFilter == "All") {
            claimsList
        } else {
            claimsList.filter { it.status == currentFilter }
        }
        adapter.updateData(filteredList)
    }

    private fun updateClaimStatus(claim: ClaimModel, newStatus: String) {
        database.getReference("claims").child(claim.claimId).child("status").setValue(newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Claim $newStatus", Toast.LENGTH_SHORT).show()
                
                // If approved, we might want to mark the item as resolved too
                if (newStatus == "Approved") {
                    database.getReference("items").child(claim.itemId).child("status").setValue("Resolved")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update claim", Toast.LENGTH_SHORT).show()
            }
    }
}