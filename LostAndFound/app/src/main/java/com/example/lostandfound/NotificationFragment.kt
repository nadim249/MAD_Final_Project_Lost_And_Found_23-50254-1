package com.example.lostandfound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Context
import android.graphics.Color
import android.util.Log

import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
class NotificationFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var lvNotifications: ListView
    private lateinit var tvPlaceholder: TextView
    private val notificationList = mutableListOf<NotificationModel>()
    private lateinit var adapter: NotificationListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        lvNotifications = view.findViewById(R.id.lvNotifications)
        tvPlaceholder = view.findViewById(R.id.tvNoNotificationsPlaceholder)

        adapter = NotificationListAdapter(requireContext(), notificationList)
        lvNotifications.adapter = adapter

        lvNotifications.setOnItemClickListener { _, _, position, _ ->
            val clickedItem = notificationList[position]
            val intent = android.content.Intent(requireContext(), ItemDetailsActivity::class.java).apply {
                putExtra("ITEM_ID", clickedItem.referenceId)
            }
            startActivity(intent)
        }

        listenForNewPostNotifications()
    }

    private fun listenForNewPostNotifications() {
        try {
            val currentUserId = auth.currentUser?.uid ?: return

            val newPostsQuery = database.getReference("notifications")
                .child(currentUserId)
                .orderByChild("type")
                .equalTo("NewPost")

            newPostsQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        notificationList.clear()

                        for (notificationSnapshot in snapshot.children) {
                            val notifId = notificationSnapshot.key ?: ""
                            val title = notificationSnapshot.child("title").value?.toString() ?: ""
                            val body = notificationSnapshot.child("body").value?.toString() ?: ""
                            val type = notificationSnapshot.child("type").value?.toString() ?: ""
                            val referenceId = notificationSnapshot.child("referenceId").value?.toString() ?: ""
                            val timestamp = notificationSnapshot.child("timestamp").value as? Long ?: 0L

                            val modelInstance = NotificationModel(notifId, title, body, timestamp, type, referenceId)
                            notificationList.add(modelInstance)
                        }

                        // Keep newest posts at the very top
                        notificationList.sortByDescending { it.timestamp }
                        adapter.notifyDataSetChanged()

                        // Manage empty list state
                        if (notificationList.isEmpty()) {
                            tvPlaceholder.visibility = View.VISIBLE
                            lvNotifications.visibility = View.GONE
                        } else {
                            tvPlaceholder.visibility = View.GONE
                            lvNotifications.visibility = View.VISIBLE
                        }

                    } catch (e: Exception) {
                        Log.e("NotificationsFragment", "Error filtering dataset", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationsFragment", "Database query canceled: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("NotificationsFragment", "Unexpected error in query setup", e)
        }
    }

    inner class NotificationListAdapter(context: Context, private val items: List<NotificationModel>) :
        ArrayAdapter<NotificationModel>(context, R.layout.item_notification_row, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rowView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_notification_row, parent, false)
            val notificationItem = items[position]

            val cardBadge = rowView.findViewById<MaterialCardView>(R.id.cardNotificationBadge)
            val tvEmoji = rowView.findViewById<TextView>(R.id.tvNotificationEmoji)
            val tvTitle = rowView.findViewById<TextView>(R.id.tvNotificationRowTitle)
            val tvBody = rowView.findViewById<TextView>(R.id.tvNotificationRowBody)

            tvTitle.text = notificationItem.title
            tvBody.text = notificationItem.body

            if (notificationItem.title.contains("Found", ignoreCase = true)) {
                cardBadge.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                tvEmoji.text = "📦"
            } else {
                cardBadge.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                tvEmoji.text = "🔍"
            }

            return rowView
        }
    }
}