package com.example.lostandfound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


data class ChatMessage(
    val text: String,
    val time: String,
    val isSent: Boolean
)

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_RECEIVED = 0
        private const val VIEW_TYPE_SENT = 1
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText: TextView = view.findViewById(R.id.tvMessageText)
        val tvTime: TextView = view.findViewById(R.id.tvTimestamp)
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText: TextView = view.findViewById(R.id.tvMessageText)
        val tvTime: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun getItemViewType(position: Int) =
        if (messages[position].isSent) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            SentViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
            )
        } else {
            ReceivedViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is SentViewHolder -> {
                holder.tvText.text = msg.text
                holder.tvTime.text = msg.time
            }
            is ReceivedViewHolder -> {
                holder.tvText.text = msg.text
                holder.tvTime.text = msg.time
            }
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }
}