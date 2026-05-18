package com.example.lostandfound

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

class ItemDetailsActivity : AppCompatActivity() {

    private lateinit var item: ItemModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_details)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            @Suppress("DEPRECATION")
            val passedItem = intent.getSerializableExtra("ITEM_DATA") as? ItemModel
            if (passedItem == null) {
                Toast.makeText(this, "Error loading item details.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            item = passedItem
        } catch (e: Exception) {
            Log.e("ItemDetailsActivity", "Error extracting intent data", e)
            finish()
            return
        }

        bindViewsAndData()
    }

    private fun bindViewsAndData() {
        try {
            // View Hooks
            val ivItemImage = findViewById<ImageView>(R.id.ivItemImage)
            val btnBack = findViewById<View>(R.id.btnBack)
            val btnShare = findViewById<View>(R.id.btnShare)

            val badgeStatus = findViewById<MaterialCardView>(R.id.badgeStatus)
            val tvStatus = findViewById<TextView>(R.id.tvStatus)

            val tvItemTitle = findViewById<TextView>(R.id.tvItemTitle)
            val tvTag1 = findViewById<TextView>(R.id.tvTag1)
            val tvTag2 = findViewById<TextView>(R.id.tvTag2)

            val tvPosterInitials = findViewById<TextView>(R.id.tvPosterInitials)
            val tvPosterName = findViewById<TextView>(R.id.tvPosterName)
            val tvPosterDept = findViewById<TextView>(R.id.tvPosterDept)

            val tvLocation = findViewById<TextView>(R.id.tvLocation)
            val tvWhen = findViewById<TextView>(R.id.tvWhen)
            val tvDescription = findViewById<TextView>(R.id.tvDescription)

            val tvColor = findViewById<TextView>(R.id.tvColor)
            val tvBrand = findViewById<TextView>(R.id.tvBrand)
            val tvSize = findViewById<TextView>(R.id.tvSize)
            val tvItemStatus = findViewById<TextView>(R.id.tvItemStatus)

            val btnReport = findViewById<View>(R.id.btnReport)
            val btnContactOwner = findViewById<View>(R.id.btnContactOwner)
            val btnClaimItem = findViewById<MaterialCardView>(R.id.btnClaimItem)
            val btnResolve = findViewById<MaterialCardView>(R.id.btnResolve)

            // Populate Core Item Data
            tvItemTitle.text = item.title
            tvDescription.text = item.description
            tvLocation.text = item.location
            tvWhen.text = item.date
            tvColor.text = if (item.color.isNotEmpty()) item.color else "N/A"
            tvBrand.text = if (item.brand.isNotEmpty()) item.brand else "N/A"
            tvSize.text = if (item.size.isNotEmpty()) item.size else "N/A"
            tvItemStatus.text = item.status

            if (item.status == "Resolved") {
                tvItemStatus.setTextColor(Color.GRAY)
                btnClaimItem.visibility = View.GONE
                btnContactOwner.visibility = View.GONE
            }

            // Initialize Poster Data with item fields (fallback/quick load)
            tvPosterName.text = "Loading..."
            tvPosterInitials.text = "??"
            tvPosterDept.text = item.postedByEmail // Show email while loading full profile


            // Tags
            tvTag1.text = item.type
            tvTag2.text = if (item.brand.isNotEmpty()) item.brand else "General"

            // Dynamic Styling
            if (item.type == "Lost") {
                badgeStatus.setCardBackgroundColor(Color.parseColor("#E57373"))
                tvStatus.text = "Lost Item"
            } else {
                badgeStatus.setCardBackgroundColor(Color.parseColor("#4CAF82"))
                tvStatus.text = "Found Item"
            }

            // --- DYNAMIC BASE64 IMAGE LOADING ---
            if (item.imagePath.length > 50) {
                try {
                    val imageByteArray = Base64.decode(item.imagePath, Base64.DEFAULT)
                    Glide.with(this)
                        .asBitmap()
                        .load(imageByteArray)
                        .centerCrop()
                        .placeholder(R.drawable.bell)
                        .into(ivItemImage)
                } catch (e: Exception) {
                    Log.e("ItemDetailsActivity", "Error decoding Base64 image", e)
                    ivItemImage.setImageResource(R.drawable.bell)
                }
            } else {
                val resourceId = resources.getIdentifier(item.imagePath, "drawable", packageName)
                if (resourceId != 0) {
                    ivItemImage.setImageResource(resourceId)
                } else {
                    ivItemImage.setImageResource(R.drawable.bell)
                }
            }

            // Hide Action Buttons if user owns this post
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == item.postedBy) {
                btnContactOwner.visibility = View.GONE
                btnClaimItem.visibility = View.GONE
                if (item.status != "Resolved") {
                    btnResolve.visibility = View.VISIBLE
                } else {
                    btnResolve.visibility = View.GONE
                }
            } else {
                btnResolve.visibility = View.GONE
            }

            btnResolve.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Mark as Resolved")
                    .setMessage("Has this item been returned to its owner?")
                    .setPositiveButton("Yes, Resolved") { _, _ ->
                        markItemAsResolved()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Fetch Poster Details from Firebase
            if (item.postedBy.isNotEmpty()) {
                database.getReference("users").child(item.postedBy)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                if (snapshot.exists()) {
                                    tvPosterName.text = snapshot.child("name").value?.toString() ?: "Unknown User"
                                    tvPosterInitials.text = snapshot.child("initials").value?.toString() ?: "??"
                                    tvPosterDept.text = snapshot.child("department").value?.toString() ?: "Department Not Specified"
                                }
                            } catch (e: Exception) {
                                Log.e("ItemDetails", "Error parsing poster data", e)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            // Button Listeners
            btnBack.setOnClickListener { finish() }

            btnShare.setOnClickListener {
                val shareText = "Check out this ${item.type} item on the Campus Lost & Found App!\n" +
                        "Item: ${item.title}\n" +
                        "Location: ${item.location}\n" +
                        "Date: ${item.date}"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Item"))
            }

            btnContactOwner.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("name", tvPosterName.text.toString())
                startActivity(intent)
            }

            btnClaimItem.setOnClickListener {
                ClaimItemDialog.newInstance()
                    .show(supportFragmentManager, ClaimItemDialog.TAG)
            }

            btnReport.setOnClickListener {
                Toast.makeText(this, "Item reported to Admin.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("ItemDetailsActivity", "Error binding views", e)
            Toast.makeText(this, "Error loading UI elements", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markItemAsResolved() {
        val currentUserId = auth.currentUser?.uid ?: return
        val itemRef = database.getReference("items").child(item.id)

        val updates = hashMapOf<String, Any>(
            "status" to "Resolved"
        )

        itemRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Increment user's resolved count
                database.getReference("users").child(currentUserId).child("stats").child("resolved")
                    .setValue(ServerValue.increment(1))

                Toast.makeText(this, "Item marked as resolved!", Toast.LENGTH_SHORT).show()
                findViewById<TextView>(R.id.tvItemStatus).text = "Resolved"
                findViewById<TextView>(R.id.tvItemStatus).setTextColor(Color.GRAY)
                findViewById<View>(R.id.btnResolve).visibility = View.GONE
            } else {
                Toast.makeText(this, "Failed to update: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}