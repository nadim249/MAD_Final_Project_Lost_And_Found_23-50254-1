package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var reportsAdapter: ReportsAdapter
    private val reportsList = mutableListOf<ReportModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reports)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance()
        setupRecyclerView()
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        fetchReports()
    }

    private fun setupRecyclerView() {
        val rvReports = findViewById<RecyclerView>(R.id.rvReports)
        reportsAdapter = ReportsAdapter(reportsList) { report, action ->
            when (action) {
                "VIEW" -> {
                    val intent = Intent(this, AdminItemDetailsActivity::class.java)
                    intent.putExtra("ITEM_ID", report.itemId)
                    startActivity(intent)
                }
                "DISMISS" -> dismissReport(report)
                "DELETE_ITEM" -> confirmDeleteItem(report)
            }
        }
        rvReports.layoutManager = LinearLayoutManager(this)
        rvReports.adapter = reportsAdapter
    }

    private fun fetchReports() {
        database.getReference("reports").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportsList.clear()
                for (reportSnap in snapshot.children) {
                    val report = reportSnap.getValue(ReportModel::class.java)
                    if (report != null && report.status == "Pending") {
                        report.id = reportSnap.key ?: ""
                        reportsList.add(report)
                    }
                }
                reportsAdapter.updateData(reportsList)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun dismissReport(report: ReportModel) {
        database.getReference("reports").child(report.id).child("status").setValue("Reviewed")
            .addOnSuccessListener {
                Toast.makeText(this, "Report dismissed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDeleteItem(report: ReportModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete Reported Item")
            .setMessage("Are you sure you want to delete this item? This will also dismiss the report.")
            .setPositiveButton("Delete") { _, _ ->
                deleteItemAndDismissReport(report)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItemAndDismissReport(report: ReportModel) {
        // Delete the item
        database.getReference("items").child(report.itemId).removeValue()
            .addOnSuccessListener {
                // Update report status
                database.getReference("reports").child(report.id).child("status").setValue("Resolved")
                Toast.makeText(this, "Item deleted and report resolved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
    }
}