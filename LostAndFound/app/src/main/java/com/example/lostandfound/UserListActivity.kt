package com.example.lostandfound

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UserListActivity : AppCompatActivity() {
    private lateinit var rvUsers: RecyclerView
    private lateinit var ivBack: View

    // Sample data — replace with real API/DB data
    private val users = listOf(
        UserModel("Sarah Martinez", "sarah@university.edu", "SM", "Lost: 3 · Found: 12", "Active"),
        UserModel("John Davis",     "john@university.edu",  "JD", "Lost: 1 · Found: 5",  "Active"),
        UserModel("Emily Chen",     "emily@university.edu", "EC", "Lost: 0 · Found: 8",  "Active"),
        UserModel("Mike Johnson",   "mike@university.edu",  "MJ", "Lost: 2 · Found: 0",  "Suspended"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivBack  = findViewById(R.id.ivBack)
        rvUsers = findViewById(R.id.rvUsers)

        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = UserListAdapter(users)

        ivBack.setOnClickListener { finish() }
    }
}

data class UserModel(
    val name: String,
    val email: String,
    val initials: String,
    val stats: String,
    val status: String
)