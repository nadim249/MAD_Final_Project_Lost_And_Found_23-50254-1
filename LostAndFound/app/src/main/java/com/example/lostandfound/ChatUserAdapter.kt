package com.example.lostandfound

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatUserAdapter(
    private val users: List<ChatUser>
) : RecyclerView.Adapter<ChatUserAdapter.ChatUserViewHolder>() {

    inner class ChatUserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitials: TextView = view.findViewById(R.id.tvInitials)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_user, parent, false)

        return ChatUserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ChatUserViewHolder, position: Int) {

        val user = users[position]

        holder.tvInitials.text = user.initials
        holder.tvName.text = user.name
        holder.tvLastMessage.text = user.lastMessage
        holder.tvTime.text = user.time

        holder.itemView.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)

            context.startActivity(intent)
        }
    }
}