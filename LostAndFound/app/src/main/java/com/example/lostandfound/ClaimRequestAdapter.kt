package com.example.lostandfound

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

data class ClaimRequest(
    val itemTitle: String,
    val claimantName: String,
    val verificationText: String,
    val timeAgo: String,
    val status: String   // "Pending", "Approved", "Rejected"
)

class ClaimRequestAdapter(
    private val items: MutableList<ClaimRequest>,
    private val onApprove: (ClaimRequest, Int) -> Unit,
    private val onReject: (ClaimRequest, Int) -> Unit
) : RecyclerView.Adapter<ClaimRequestAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemTitle:    TextView         = view.findViewById(R.id.tvItemTitle)
        val tvClaimantName: TextView         = view.findViewById(R.id.tvClaimantName)
        val tvVerification: TextView         = view.findViewById(R.id.tvVerification)
        val tvClaimTime:    TextView         = view.findViewById(R.id.tvClaimTime)
        val tvClaimStatus:  TextView         = view.findViewById(R.id.tvClaimStatus)
        val statusBadge:    MaterialCardView = view.findViewById(R.id.claimStatusBadge)
        val btnApprove:     View             = view.findViewById(R.id.btnApprove)
        val btnReject:      View             = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_claim_req_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvItemTitle.text    = item.itemTitle
        holder.tvClaimantName.text = item.claimantName
        holder.tvVerification.text = item.verificationText
        holder.tvClaimTime.text    = item.timeAgo
        holder.tvClaimStatus.text  = item.status

        // Style badge + show/hide action buttons based on status
        when (item.status) {
            "Approved" -> {
                holder.tvClaimStatus.setTextColor(Color.parseColor("#4CAF82"))
                holder.statusBadge.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility  = View.GONE
            }
            "Rejected" -> {
                holder.tvClaimStatus.setTextColor(Color.parseColor("#E57373"))
                holder.statusBadge.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility  = View.GONE
            }
            else -> { // Pending
                holder.tvClaimStatus.setTextColor(Color.parseColor("#FF9800"))
                holder.statusBadge.setCardBackgroundColor(Color.parseColor("#FFF8E1"))
                holder.btnApprove.visibility = View.VISIBLE
                holder.btnReject.visibility  = View.VISIBLE
            }
        }

        holder.btnApprove.setOnClickListener {
            onApprove(item, holder.adapterPosition)
        }
        holder.btnReject.setOnClickListener {
            onReject(item, holder.adapterPosition)
        }
    }

    override fun getItemCount() = items.size

    // Update a single item status and refresh that row only
    fun updateStatus(position: Int, newStatus: String) {
        if (position < 0 || position >= items.size) return
        items[position] = items[position].copy(status = newStatus)
        notifyItemChanged(position)
    }
}