package com.example.lostandfound

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class RecentItemsAdapter(private val items: List<ItemModel>) :
    RecyclerView.Adapter<RecentItemsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootCard: MaterialCardView = view as MaterialCardView
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvItemDesc)
        val tvLocation: TextView = view.findViewById(R.id.tvItemLocation)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val tvBadgeText: TextView = view.findViewById(R.id.tvBadgeText)
        val badgeStatus: MaterialCardView = view.findViewById(R.id.badgeStatus)
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

        // Click Listener for Item Details
        holder.rootCard.setOnClickListener {
            val intent = Intent(context, ItemDetailsActivity::class.java)
            intent.putExtra("ITEM_DATA", item) // Pass object to details screen
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}