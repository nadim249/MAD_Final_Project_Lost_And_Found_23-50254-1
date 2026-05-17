package com.example.lostandfound

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class ItemsListActivity : AppCompatActivity() {
    private lateinit var rvItems: RecyclerView
    private lateinit var ivBack: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_items_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivBack  = findViewById(R.id.ivBack)
        rvItems = findViewById(R.id.rvItems)

        // Reuse the same adapter from HomeFragment (ChatAdapter style)
        // rvItems.layoutManager = LinearLayoutManager(this)
        // rvItems.adapter = ItemsAdapter(itemsList)

        ivBack.setOnClickListener { finish() }
    }
}