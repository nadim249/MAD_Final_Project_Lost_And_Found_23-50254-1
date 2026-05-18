package com.example.lostandfound

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val iconBox: MaterialCardView = view.findViewById(R.id.iconBox)
        val ivIcon: ImageView = view.findViewById(R.id.ivItemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_recent_row, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description
        holder.tvLocation.text = item.location
        holder.tvDate.text = item.date

        // Dynamic Styling based on Status
        if (item.type == "Lost") {
            holder.badgeStatus.setCardBackgroundColor(Color.parseColor("#7B5EA7"))
            holder.tvBadgeText.text = "Lost"
            holder.iconBox.setCardBackgroundColor(Color.parseColor("#EEF2FF"))
            holder.ivIcon.setColorFilter(Color.parseColor("#6B8DD6"))
        } else {
            holder.badgeStatus.setCardBackgroundColor(Color.parseColor("#4CAF82"))
            holder.tvBadgeText.text = "Found"
            holder.iconBox.setCardBackgroundColor(Color.parseColor("#E8EAF6"))
            holder.ivIcon.setColorFilter(Color.parseColor("#5C6BC0"))
        }

        // Dynamic Image Loading
        val context = holder.itemView.context
        val resourceId = context.resources.getIdentifier(item.imagePath, "drawable", context.packageName)
        if (resourceId != 0) {
            holder.ivIcon.setImageResource(resourceId)
        } else {
            holder.ivIcon.setImageResource(R.drawable.bell) // Safe fallback
        }

        holder.rootCard.setOnClickListener {
            val intent = Intent(context, ItemDetailsActivity::class.java)
            intent.putExtra("ITEM_DATA", item) // Pass object to details screen
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}