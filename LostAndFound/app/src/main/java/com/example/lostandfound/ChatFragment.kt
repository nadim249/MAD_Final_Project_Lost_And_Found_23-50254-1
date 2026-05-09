package com.example.lostandfound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: View
    private lateinit var adapter: ChatAdapter

    private val messages = mutableListOf(
        ChatMessage("Hi! I think I found your backpack!", "10:30 AM", isSent = false),
        ChatMessage("Really? That's great! Where did you find it?", "10:32 AM", isSent = true),
        ChatMessage("In the library on the 2nd floor, near study area B", "10:33 AM", isSent = false),
        ChatMessage("Yes! That's exactly where I lost it. Does it have a blue keychain?", "10:35 AM", isSent = true),
        ChatMessage("Yes it does! And there's a MacBook inside", "10:36 AM", isSent = false),
        ChatMessage("Perfect! When can we meet? I really need my laptop back for tomorrow", "10:37 AM", isSent = true),
        ChatMessage("I'm at the student center now. Can you come here?", "10:38 AM", isSent = false),
        ChatMessage("On my way! I'll be there in 10 minutes. Thank you so much!", "10:40 AM", isSent = true),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        rvMessages = view.findViewById(R.id.rvMessages)
        etMessage  = view.findViewById(R.id.etMessage)
        btnSend    = view.findViewById(R.id.btnSend)

        // Set up RecyclerView
        adapter = ChatAdapter(messages)
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        rvMessages.layoutManager = layoutManager
        rvMessages.adapter = adapter

        // Send button
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                adapter.addMessage(ChatMessage(text, time, isSent = true))
                etMessage.setText("")
                rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }



        // Auto-scroll when keyboard pushes layout up
        rvMessages.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }
}