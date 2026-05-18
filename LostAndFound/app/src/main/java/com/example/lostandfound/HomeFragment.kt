package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var rvRecentItems: RecyclerView
    private lateinit var adapter: RecentItemsAdapter

    private var allItems = mutableListOf<ItemModel>()
    private var filteredItems = mutableListOf<ItemModel>()

    private var currentFilter = "All"
    private var currentSearchQuery = ""

    private lateinit var filterAll: TextView
    private lateinit var filterLost: TextView
    private lateinit var filterFound: TextView
    private lateinit var etSearch: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val avatarCard = view.findViewById<View>(R.id.avatarCard)
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val tvAvatarInitials = avatarCard.findViewById<TextView>(R.id.tvAvatarInitials)

        filterAll = view.findViewById(R.id.filterAll)
        filterLost = view.findViewById(R.id.filterLost)
        filterFound = view.findViewById(R.id.filterFound)
        etSearch = view.findViewById(R.id.etSearch)

        rvRecentItems = view.findViewById(R.id.rvRecentItems)

        // Setup RecyclerView
        rvRecentItems.layoutManager = LinearLayoutManager(requireContext())
        adapter = RecentItemsAdapter(filteredItems)
        rvRecentItems.adapter = adapter

        // Fetch User Greeting

        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                if (snapshot.exists()) {
                                    val name = snapshot.child("name").value?.toString() ?: "Student"
                                    val initials = snapshot.child("initials").value?.toString() ?: "??"
                                    val firstName = name.split(" ").firstOrNull() ?: "Student"

                                    tvGreeting.text = "Hello, $firstName! 👋"
                                    tvAvatarInitials?.text = initials
                                }
                            } catch (e: Exception) {
                                Log.e("HomeFragment", "Error parsing user data", e)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error fetching user", e)
        }

        fetchItems()


        avatarCard.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        filterAll.setOnClickListener { applyFilter("All") }
        filterLost.setOnClickListener { applyFilter("Lost") }
        filterFound.setOnClickListener { applyFilter("Found") }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString().trim()
                updateDisplayedItems()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchItems() {
        try {
            FirebaseDatabase.getInstance().getReference("items")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            allItems.clear()

                            for (itemSnapshot in snapshot.children) {
                                val item = itemSnapshot.getValue(ItemModel::class.java)
                                if (item != null) {
                                    item.id = itemSnapshot.key ?: ""
                                    // Only show Active items in Recent Items
                                    if (item.status == "Active") {
                                        allItems.add(item)
                                    }
                                }
                            }

                            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                            allItems.sortByDescending { item ->
                                try {
                                    dateFormat.parse(item.date)?.time ?: 0L
                                } catch (e: Exception) {
                                    0L
                                }
                            }

                            updateDisplayedItems()

                        } catch (e: Exception) {
                            Log.e("HomeFragment", "Error parsing item data", e)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (context != null) {
                            Toast.makeText(requireContext(), "Failed to load items.", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error executing item fetch", e)
        }
    }

    private fun applyFilter(filterType: String) {
        currentFilter = filterType

        filterAll.setBackgroundResource(R.drawable.pill_inactive_bg)
        filterLost.setBackgroundResource(R.drawable.pill_inactive_bg)
        filterFound.setBackgroundResource(R.drawable.pill_inactive_bg)

        filterAll.setTextColor(resources.getColor(R.color.textSecondary, null))
        filterLost.setTextColor(resources.getColor(R.color.textSecondary, null))
        filterFound.setTextColor(resources.getColor(R.color.textSecondary, null))

        // Set Active Pill Style
        when (filterType) {
            "All" -> {
                filterAll.setBackgroundResource(R.drawable.pill_active_bg)
                filterAll.setTextColor(resources.getColor(R.color.white, null))
            }
            "Lost" -> {
                filterLost.setBackgroundResource(R.drawable.pill_active_bg)
                filterLost.setTextColor(resources.getColor(R.color.white, null))
            }
            "Found" -> {
                filterFound.setBackgroundResource(R.drawable.pill_active_bg)
                filterFound.setTextColor(resources.getColor(R.color.white, null))
            }
        }

        updateDisplayedItems()
    }

    // BOTH Filter and Search
    private fun updateDisplayedItems() {
        filteredItems.clear()

        val statusFilteredList = if (currentFilter == "All") {
            allItems
        } else {
            allItems.filter { it.type == currentFilter }
        }

        if (currentSearchQuery.isEmpty()) {
            filteredItems.addAll(statusFilteredList)
        } else {
            filteredItems.addAll(statusFilteredList.filter { item ->
                item.title.contains(currentSearchQuery, ignoreCase = true) ||
                        item.description.contains(currentSearchQuery, ignoreCase = true) ||
                        item.location.contains(currentSearchQuery, ignoreCase = true)
            })
        }

        adapter.notifyDataSetChanged()
    }
}