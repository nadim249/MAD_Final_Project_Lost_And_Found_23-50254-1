package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserListActivity : AppCompatActivity() {
    private lateinit var rvUsers: RecyclerView
    private lateinit var ivBack: View
    private lateinit var etSearch: EditText
    private lateinit var tvUserCount: TextView
    
    private lateinit var database: FirebaseDatabase
    private val allUsers = mutableListOf<UserModel>()
    private val filteredUsersList = mutableListOf<UserModel>()
    private lateinit var adapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_list)
        
        database = FirebaseDatabase.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivBack  = findViewById(R.id.ivBack)
        rvUsers = findViewById(R.id.rvUsers)
        etSearch = findViewById(R.id.etSearch)
        tvUserCount = findViewById(R.id.tvUserCount)

        rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = UserListAdapter(filteredUsersList) { user ->
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("USER_DATA", user)
            startActivity(intent)
        }
        rvUsers.adapter = adapter

        ivBack.setOnClickListener { finish() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        fetchUsers()
    }

    private fun filterUsers(query: String) {
        val filtered = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.email.contains(query, ignoreCase = true) 
            }
        }
        
        filteredUsersList.clear()
        filteredUsersList.addAll(filtered)
        adapter.notifyDataSetChanged()
        tvUserCount.text = "${filteredUsersList.size} Users"
    }

    private fun fetchUsers() {
        database.getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allUsers.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserModel::class.java)?.copy(uid = userSnapshot.key ?: "")
                    if (user != null) {
                        allUsers.add(user)
                    }
                }
                filterUsers(etSearch.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}