package com.example.lostandfound

import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminItemDetailsActivity : AppCompatActivity() {

    private lateinit var item: ItemModel
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_details)
        database = FirebaseDatabase.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val passedItem = intent.getSerializableExtra("ITEM_DATA") as? ItemModel
        val itemId = intent.getStringExtra("ITEM_ID")

        if (passedItem != null) {
            item = passedItem
            bindViewsAndData()
        } else if (itemId != null) {
            fetchItemAndBind(itemId)
        } else {
            Toast.makeText(this, "Error: No item data found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchItemAndBind(itemId: String) {
        database.getReference("items").child(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedItem = snapshot.getValue(ItemModel::class.java)
                    if (fetchedItem != null) {
                        fetchedItem.id = snapshot.key ?: itemId
                        item = fetchedItem
                        bindViewsAndData()
                    } else {
                        Toast.makeText(this@AdminItemDetailsActivity, "Item not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminItemDetailsActivity, "Database error", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    private fun bindViewsAndData() {
        val ivItemImage = findViewById<ImageView>(R.id.ivItemImage)
        val btnBack = findViewById<View>(R.id.btnBack)
        val badgeStatus = findViewById<MaterialCardView>(R.id.badgeStatus)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvItemTitle = findViewById<TextView>(R.id.tvItemTitle)
        val tvPosterName = findViewById<TextView>(R.id.tvPosterName)
        val tvPosterInitials = findViewById<TextView>(R.id.tvPosterInitials)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvWhen = findViewById<TextView>(R.id.tvWhen)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvItemStatus = findViewById<TextView>(R.id.tvItemStatus)
        
        // Admin specific: hide user action buttons
        findViewById<View>(R.id.btnReport).visibility = View.GONE
        findViewById<View>(R.id.btnContactOwner).visibility = View.GONE
        findViewById<View>(R.id.btnClaimItem).visibility = View.GONE
        findViewById<View>(R.id.btnResolve).visibility = View.GONE
        findViewById<View>(R.id.cardOfficeStatus).visibility = View.GONE
        
        val btnDelete = findViewById<View>(R.id.btnDelete)
        btnDelete.visibility = View.VISIBLE

        tvItemTitle.text = item.title
        tvDescription.text = item.description
        tvLocation.text = item.location
        tvWhen.text = item.date
        tvItemStatus.text = item.status

        if (item.type == "Lost") {
            badgeStatus.setCardBackgroundColor(Color.parseColor("#E57373"))
            tvStatus.text = "Lost Item"
        } else {
            badgeStatus.setCardBackgroundColor(Color.parseColor("#4CAF82"))
            tvStatus.text = "Found Item"
        }

        if (item.imagePath.isNotEmpty()) {
            if (item.imagePath.length > 50) {
                val imageByteArray = Base64.decode(item.imagePath, Base64.DEFAULT)
                Glide.with(this).asBitmap().load(imageByteArray).into(ivItemImage)
            } else {
                val resourceId = resources.getIdentifier(item.imagePath, "drawable", packageName)
                if (resourceId != 0) ivItemImage.setImageResource(resourceId)
            }
        }

        database.getReference("users").child(item.postedBy).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvPosterName.text = snapshot.child("name").value?.toString() ?: "Unknown"
                tvPosterInitials.text = snapshot.child("initials").value?.toString() ?: "??"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        btnBack.setOnClickListener { finish() }
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Delete this item permanently?")
                .setPositiveButton("Delete") { _, _ ->
                    database.getReference("items").child(item.id).removeValue().addOnSuccessListener {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}