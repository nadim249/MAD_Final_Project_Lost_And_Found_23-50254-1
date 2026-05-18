package com.example.lostandfound

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ItemsListActivity : AppCompatActivity() {
    private lateinit var rvItems: RecyclerView
    private lateinit var ivBack: View
    private lateinit var database: FirebaseDatabase
    private val itemsList = mutableListOf<ItemModel>()
    private lateinit var adapter: RecentItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_items_list)
        
        database = FirebaseDatabase.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivBack  = findViewById(R.id.ivBack)
        rvItems = findViewById(R.id.rvItems)

        rvItems.layoutManager = LinearLayoutManager(this)
        adapter = RecentItemsAdapter(itemsList, isAdmin = true)
        rvItems.adapter = adapter

        ivBack.setOnClickListener { finish() }
        
        fetchItems()
    }

    private fun fetchItems() {
        database.getReference("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemsList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(ItemModel::class.java)
                    if (item != null) {
                        item.id = itemSnapshot.key ?: ""
                        itemsList.add(item)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}