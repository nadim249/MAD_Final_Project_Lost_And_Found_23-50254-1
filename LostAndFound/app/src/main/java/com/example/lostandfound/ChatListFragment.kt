package com.example.lostandfound


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatListFragment : Fragment() {

    private lateinit var rvChatList: RecyclerView
    private val threadsList = mutableListOf<ChatThreadModel>()
    private lateinit var adapter: ThreadAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        rvChatList = view.findViewById(R.id.rvChatList)
        rvChatList.layoutManager = LinearLayoutManager(requireContext())
        adapter = ThreadAdapter(threadsList)
        rvChatList.adapter = adapter

        fetchChatThreads()
    }
    private fun fetchChatThreads() {
        val uid = auth.currentUser?.uid ?: return

        database.getReference("chat_lists").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    threadsList.clear()
                    for (threadSnapshot in snapshot.children) {
                        val thread = threadSnapshot.getValue(ChatThreadModel::class.java)
                        if (thread != null) threadsList.add(thread)
                    }
                    threadsList.sortByDescending { it.timestamp }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
    inner class ThreadAdapter(private val list: List<ChatThreadModel>) :
        RecyclerView.Adapter<ThreadAdapter.Holder>() {

        inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
            val name = v.findViewById<TextView>(R.id.tvThreadName)
            val msg = v.findViewById<TextView>(R.id.tvThreadLastMsg)
            val initials = v.findViewById<TextView>(R.id.tvThreadInitials)
        }

        override fun onCreateViewHolder(p: ViewGroup, t: Int) =
            Holder(LayoutInflater.from(p.context).inflate(R.layout.item_chat_thread, p, false))

        override fun onBindViewHolder(h: Holder, pos: Int) {
            val t = list[pos]
            h.name.text = t.peerName
            h.msg.text = t.lastMessage

            h.initials.text = t.peerName.split(' ')
                .mapNotNull { it.firstOrNull()?.toString() }
                .joinToString("").take(2).uppercase()

            h.itemView.setOnClickListener {
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra("PEER_ID", t.peerId)
                    putExtra("PEER_NAME", t.peerName)
                }
                startActivity(intent)
            }
        }
        override fun getItemCount() = list.size
    }
}