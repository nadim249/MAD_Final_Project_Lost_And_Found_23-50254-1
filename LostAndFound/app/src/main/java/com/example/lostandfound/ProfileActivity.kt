package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    lateinit var backbtn: ImageView

    private lateinit var tvAvatarInitials: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvDepartment: TextView
    private lateinit var tvItemsLost: TextView
    private lateinit var tvItemsFound: TextView
    private lateinit var tvResolved: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvCampus: TextView
    private lateinit var ivBack: View
    private lateinit var cardSignOut: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials)
        tvFullName       = findViewById(R.id.tvFullName)
        tvDepartment     = findViewById(R.id.tvDepartment)
        tvItemsLost      = findViewById(R.id.tvItemsLost)
        tvItemsFound     = findViewById(R.id.tvItemsFound)
        tvResolved       = findViewById(R.id.tvResolved)
        tvName           = findViewById(R.id.tvName)
        tvEmail          = findViewById(R.id.tvEmail)
        tvPhone          = findViewById(R.id.tvPhone)
        tvCampus         = findViewById(R.id.tvCampus)
        ivBack           = findViewById(R.id.ivBack)
        cardSignOut      = findViewById(R.id.cardSignOut)

        // Populate data
        tvAvatarInitials.text = "SM"
        tvFullName.text       = "Sarah Martinez"
        tvDepartment.text     = "Computer Science • Junior"
        tvItemsLost.text      = "3"
        tvItemsFound.text     = "12"
        tvResolved.text       = "8"
        tvName.text           = "Sarah Martinez"
        tvEmail.text          = "sarah.martinez@university.edu"
        tvPhone.text          = "+1 (555) 123-4567"
        tvCampus.text         = "Main Campus – North Hall"

        // Back button
        ivBack.setOnClickListener {
            finish()
        }



        // Sign Out
// Make sure to import this at the top:
// import com.google.firebase.auth.FirebaseAuth

        cardSignOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->

                    // 1. Sign out of Firebase
                    FirebaseAuth.getInstance().signOut()

                    // 2. Route back to Login screen
                    val intent = Intent(this, LoginActivity::class.java)

                    // 3. CLEAR the backstack so they can't press "Back" to re-enter
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}