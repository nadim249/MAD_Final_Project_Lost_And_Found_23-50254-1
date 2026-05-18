package com.example.lostandfound

import android.graphics.Typeface
import android.widget.Toast
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ItemsListActivity : AppCompatActivity() {
    private lateinit var rvItems: RecyclerView
    private lateinit var ivBack: View
    private lateinit var etSearch: EditText
    private lateinit var filterAll: TextView
    private lateinit var filterLost: TextView
    private lateinit var filterFound: TextView

    private lateinit var database: FirebaseDatabase
    private val allItems = mutableListOf<ItemModel>()
    private val filteredItemsList = mutableListOf<ItemModel>()
    private lateinit var adapter: RecentItemsAdapter

    private var currentTypeFilter = "All"
    private var filterStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_items_list)
        
        database = FirebaseDatabase.getInstance()
        filterStatus = intent.getStringExtra("FILTER_STATUS")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivBack = findViewById(R.id.ivBack)
        rvItems = findViewById(R.id.rvItems)
        etSearch = findViewById(R.id.etSearch)
        filterAll = findViewById(R.id.filterAll)
        filterLost = findViewById(R.id.filterLost)
        filterFound = findViewById(R.id.filterFound)

        rvItems.layoutManager = LinearLayoutManager(this)
        adapter = RecentItemsAdapter(filteredItemsList, isAdmin = true)
        rvItems.adapter = adapter

        ivBack.setOnClickListener { finish() }



        setupFilters()
        
        fetchItems()
    }

    private fun setupFilters() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        filterAll.setOnClickListener {
            updateTypeFilter("All")
        }
        filterLost.setOnClickListener {
            updateTypeFilter("Lost")
        }
        filterFound.setOnClickListener {
            updateTypeFilter("Found")
        }
    }

    private fun updateTypeFilter(type: String) {
        currentTypeFilter = type
        
        // Update UI states
        val activeBg = R.drawable.pill_active_bg
        val inactiveBg = R.drawable.pill_inactive_bg
        val activeText = Color.WHITE
        val inactiveText = Color.parseColor("#888888")

        filterAll.apply {
            background = ContextCompat.getDrawable(context, if (type == "All") activeBg else inactiveBg)
            setTextColor(if (type == "All") activeText else inactiveText)
            setTypeface(null, if (type == "All") Typeface.BOLD else Typeface.NORMAL)
        }
        filterLost.apply {
            background = ContextCompat.getDrawable(context, if (type == "Lost") activeBg else inactiveBg)
            setTextColor(if (type == "Lost") activeText else inactiveText)
            setTypeface(null, if (type == "Lost") Typeface.BOLD else Typeface.NORMAL)
        }
        filterFound.apply {
            background = ContextCompat.getDrawable(context, if (type == "Found") activeBg else inactiveBg)
            setTextColor(if (type == "Found") activeText else inactiveText)
            setTypeface(null, if (type == "Found") Typeface.BOLD else Typeface.NORMAL)
        }

        applyFilters()
    }

    private fun applyFilters() {
        val query = etSearch.text.toString().trim().lowercase()
        
        val filtered = allItems.filter { item ->
            val matchesSearch = query.isEmpty() || 
                               item.title.lowercase().contains(query) || 
                               item.description.lowercase().contains(query) ||
                               item.location.lowercase().contains(query)
            
            val matchesType = if (currentTypeFilter == "All") true 
                             else item.type.equals(currentTypeFilter, ignoreCase = true)
            
            val matchesStatus = if (filterStatus != null) {
                if (filterStatus.equals("Resolved", ignoreCase = true)) {
                    item.status.equals("Resolved", ignoreCase = true) || 
                    item.status.equals("Found", ignoreCase = true)
                } else {
                    item.status.equals(filterStatus, ignoreCase = true)
                }
            } else true

            matchesSearch && matchesType && matchesStatus
        }

        filteredItemsList.clear()
        filteredItemsList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun fetchItems() {
        database.getReference("items").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allItems.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(ItemModel::class.java)
                    if (item != null) {
                        item.id = itemSnapshot.key ?: ""
                        allItems.add(item)
                    }
                }
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}