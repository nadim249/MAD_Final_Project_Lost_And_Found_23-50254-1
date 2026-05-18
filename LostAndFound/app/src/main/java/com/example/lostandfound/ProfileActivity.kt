package com.example.lostandfound

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // State Tracking
    private var isEditModeActive = false

    // UI Hooks
    private lateinit var btnEditToggle: ImageView
    private lateinit var btnSaveProfile: MaterialCardView
    private lateinit var cardSignOut: MaterialCardView

    private lateinit var tvAvatarInitials: TextView
    private lateinit var etProfileName: EditText
    private lateinit var tvProfileRole: TextView
    private lateinit var tvUserEmail: TextView

    // Editable Fields
    private lateinit var etUserPhone: EditText
    private lateinit var etUserDept: EditText
    private lateinit var etUserCampus: EditText

    // Stats
    private lateinit var tvCountLost: TextView
    private lateinit var tvCountFound: TextView
    private lateinit var tvCountResolved: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        fetchUserProfileData()
    }

    private fun initializeViews() {
        try {
            btnEditToggle = findViewById(R.id.btnEditToggle)
            btnSaveProfile = findViewById(R.id.btnSaveProfile)
            cardSignOut = findViewById(R.id.cardSignOut)

            tvAvatarInitials = findViewById(R.id.tvAvatarInitials)
            etProfileName = findViewById(R.id.etProfileName)
            tvProfileRole = findViewById(R.id.tvProfileRole)
            tvUserEmail = findViewById(R.id.tvUserEmail)

            etUserPhone = findViewById(R.id.etUserPhone)
            etUserDept = findViewById(R.id.etUserDept)
            etUserCampus = findViewById(R.id.etUserCampus)

            tvCountLost = findViewById(R.id.tvCountLost)
            tvCountFound = findViewById(R.id.tvCountFound)
            tvCountResolved = findViewById(R.id.tvCountResolved)
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error binding view hooks", e)
        }
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        // Toggle Edit Mode interface elements
        btnEditToggle.setOnClickListener {
            try {
                if (isEditModeActive) {
                    disableEditingUI()
                } else {
                    enableEditingUI()
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Toggle state crash caught", e)
            }
        }

        btnSaveProfile.setOnClickListener {
            saveProfileChangesToFirebase()
        }

        // Sign Out
        cardSignOut.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun fetchUserProfileData() {
        try {
            val currentUid = auth.currentUser?.uid
            if (currentUid == null) {
                Log.e("ProfileActivity", "FETCH ERROR: Current user UID is null! User might not be authenticated.")
                Toast.makeText(this, "Session invalid. Relogging required.", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d("ProfileActivity", "Attempting to fetch data for UID: $currentUid")

            database.getReference("users").child(currentUid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            if (!snapshot.exists()) {
                                Log.e("ProfileActivity", "FETCH ERROR: Snapshot does not exist in Firebase for UID: $currentUid")
                                Toast.makeText(this@ProfileActivity, "User profile not found on server.", Toast.LENGTH_SHORT).show()
                                return
                            }

                            Log.d("ProfileActivity", "Raw Snapshot Data: ${snapshot.value}")

                            val name = snapshot.child("name").value?.toString() ?: "Campus Student"
                            val role = snapshot.child("role").value?.toString() ?: "User"
                            val initials = snapshot.child("initials").value?.toString() ?: "??"
                            val email = snapshot.child("email").value?.toString() ?: "No email linked"
                            val phone = snapshot.child("phone").value?.toString() ?: ""
                            val department = snapshot.child("department").value?.toString() ?: ""
                            val campus = snapshot.child("campus").value?.toString() ?: ""

                            if (!isEditModeActive) {
                                etProfileName.setText(name)
                                etUserPhone.setText(if (phone == "Not specified") "" else phone)
                                etUserDept.setText(if (department == "Not specified") "" else department)
                                etUserCampus.setText(if (campus == "Not specified") "" else campus)
                            }
                            
                            tvProfileRole.text = role
                            tvAvatarInitials.text = initials
                            tvUserEmail.text = email

                            val statsNode = snapshot.child("stats")
                            if (statsNode.exists()) {
                                val lostVal = statsNode.child("itemsLost").value?.toString() ?: "0"
                                val foundVal = statsNode.child("itemsFound").value?.toString() ?: "0"
                                val resolvedVal = statsNode.child("resolved").value?.toString() ?: "0"

                                tvCountLost.text = if (lostVal == "null") "0" else lostVal
                                tvCountFound.text = if (foundVal == "null") "0" else foundVal
                                tvCountResolved.text = if (resolvedVal == "null") "0" else resolvedVal
                            } else {
                                Log.w("ProfileActivity", "Warning: 'stats' node missing for this user. Defaulting to 0.")
                                tvCountLost.text = "0"
                                tvCountFound.text = "0"
                                tvCountResolved.text = "0"
                            }

                        } catch (e: Exception) {
                            Log.e("ProfileActivity", "CRASH during data parsing inside onDataChange", e)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ProfileActivity", "Firebase Database Error: ${error.message} (Code: ${error.code})")
                        Toast.makeText(this@ProfileActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Unexpected top-level error in fetchUserProfileData()", e)
        }
    }
    private fun enableEditingUI() {
        isEditModeActive = true
        btnEditToggle.setImageResource(R.drawable.back)
        btnEditToggle.setColorFilter(Color.parseColor("#E57373"))
        btnSaveProfile.visibility = View.VISIBLE

        val activeBg = Color.parseColor("#F0F2F9")
        etProfileName.isEnabled = true
        etProfileName.setBackgroundColor(activeBg)

        etUserPhone.isEnabled = true
        etUserPhone.setBackgroundColor(activeBg)

        etUserDept.isEnabled = true
        etUserDept.setBackgroundColor(activeBg)

        etUserCampus.isEnabled = true
        etUserCampus.setBackgroundColor(activeBg)
    }

    private fun disableEditingUI() {
        isEditModeActive = false
        btnEditToggle.setImageResource(R.drawable.baseline_edit_24)
        btnEditToggle.setColorFilter(Color.parseColor("#6B8DD6"))
        btnSaveProfile.visibility = View.GONE

        etProfileName.isEnabled = false
        etProfileName.setBackgroundColor(Color.TRANSPARENT)

        etUserPhone.isEnabled = false
        etUserPhone.setBackgroundColor(Color.TRANSPARENT)

        etUserDept.isEnabled = false
        etUserDept.setBackgroundColor(Color.TRANSPARENT)

        etUserCampus.isEnabled = false
        etUserCampus.setBackgroundColor(Color.TRANSPARENT)

        fetchUserProfileData()
    }

    private fun saveProfileChangesToFirebase() {
        try {
            val currentUid = auth.currentUser?.uid ?: return
            val newName = etProfileName.text.toString().trim()
            val newPhone = etUserPhone.text.toString().trim()
            val newDept = etUserDept.text.toString().trim()
            val newCampus = etUserCampus.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Profile name cannot be blank", Toast.LENGTH_SHORT).show()
                return
            }

            val newInitials = newName.split(' ')
                .mapNotNull { it.firstOrNull()?.toString() }
                .joinToString("").take(2).uppercase()

            val updateMap = hashMapOf<String, Any>(
                "name" to newName,
                "initials" to newInitials,
                "phone" to if (newPhone.isNotEmpty()) newPhone else "Not specified",
                "department" to if (newDept.isNotEmpty()) newDept else "Not specified",
                "campus" to if (newCampus.isNotEmpty()) newCampus else "Not specified"
            )

            database.getReference("users").child(currentUid).updateChildren(updateMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                        tvAvatarInitials.text = newInitials
                        disableEditingUI()
                    } else {
                        Toast.makeText(this, "Save failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error executing cloud sync worker task updates", e)
        }
    }
}