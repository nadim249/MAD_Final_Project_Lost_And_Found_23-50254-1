package com.example.lostandfound

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase

class RecentItemsAdapter(
    private val items: List<ItemModel>,
    private val isAdmin: Boolean = false
) :
    RecyclerView.Adapter<RecentItemsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootCard: MaterialCardView = view as MaterialCardView
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvItemDesc)
        val tvLocation: TextView = view.findViewById(R.id.tvItemLocation)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val tvBadgeText: TextView = view.findViewById(R.id.tvBadgeText)
        val badgeStatus: MaterialCardView = view.findViewById(R.id.badgeStatus)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_recent_row, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description
        holder.tvLocation.text = item.location
        holder.tvDate.text = item.date

        if (item.type == "Lost") {
            holder.badgeStatus.setCardBackgroundColor(Color.parseColor("#7B5EA7"))
            holder.tvBadgeText.text = "Lost"
        } else {
            holder.badgeStatus.setCardBackgroundColor(Color.parseColor("#4CAF82"))
            holder.tvBadgeText.text = "Found"
        }

        val context = holder.itemView.context

        if (isAdmin) {
            holder.ivDelete.visibility = View.VISIBLE
            holder.ivDelete.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        FirebaseDatabase.getInstance().getReference("items").child(item.id).removeValue()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            holder.ivDelete.visibility = View.GONE
        }

        // Click Listener for Item Details
        holder.rootCard.setOnClickListener {
            val intent = if (isAdmin) {
                Intent(context, AdminItemDetailsActivity::class.java)
            } else {
                Intent(context, ItemDetailsActivity::class.java)
            }
            intent.putExtra("ITEM_DATA", item) // Pass object to details screen
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}