package com.example.lostandfound

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class UserListAdapter(private val users: List<UserModel>) :
    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView   = view.findViewById(R.id.tvInitials)
        val tvName: TextView       = view.findViewById(R.id.tvUserName)
        val tvEmail: TextView      = view.findViewById(R.id.tvUserEmail)
        val tvStats: TextView      = view.findViewById(R.id.tvUserStats)
        val tvStatus: TextView     = view.findViewById(R.id.tvUserStatus)
        val badgeCard: MaterialCardView = view.findViewById(R.id.userBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user_row, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.tvInitials.text = user.initials
        holder.tvName.text     = user.name
        holder.tvEmail.text    = user.email
        holder.tvStats.text    = user.stats
        holder.tvStatus.text   = user.status

        if (user.status == "Suspended") {
            holder.tvStatus.setTextColor(Color.parseColor("#E57373"))
            holder.badgeCard.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF82"))
            holder.badgeCard.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
        }
    }

    override fun getItemCount() = users.size
}