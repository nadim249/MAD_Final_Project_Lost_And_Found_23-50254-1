package com.example.lostandfound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatListFragment : Fragment() {

    private lateinit var rvChats: RecyclerView

    private val users = listOf(

        ChatUser(
            "John Davis",
            "I'm at the student center now",
            "10:40 AM",
            "JD"
        ),

        ChatUser(
            "Sarah Ahmed",
            "Thanks for returning my wallet!",
            "Yesterday",
            "SA"
        ),

        ChatUser(
            "Michael Lee",
            "Did you find the charger?",
            "Monday",
            "ML"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_chat_list,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvChats = view.findViewById(R.id.rvChats)

        rvChats.layoutManager = LinearLayoutManager(requireContext())
        rvChats.adapter = ChatUserAdapter(users)
    }
}