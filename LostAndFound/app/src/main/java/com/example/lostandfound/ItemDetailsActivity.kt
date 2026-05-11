package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ItemDetailsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnBack        = findViewById<View>(R.id.btnBack)
        val btnShare       = findViewById<View>(R.id.btnShare)
        val btnClaimItem   = findViewById<View>(R.id.btnClaimItem)
        val btnReport      = findViewById<View>(R.id.btnReport)
        val btnContactOwner = findViewById<View>(R.id.btnContactOwner)

        btnBack.setOnClickListener { finish() }

        // Share
        btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, " Item: $title\nLocation: ")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Item"))
        }

        btnContactOwner.setOnClickListener {
            val chatFragment = ChatFragment()

            supportFragmentManager.beginTransaction()
                // R.id.main is the container defined in activity_item_details.xml
                .replace(R.id.main, chatFragment)
                // This allows the user to go back to the details when they press the back button
                .addToBackStack(null)
                .commit()
        }


    }
}