package com.example.lostandfound

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var peerId: String = ""
    private var peerName: String = ""
    private var currentUserId: String = ""
    private var chatRoomId: String = ""

    private lateinit var rvMessages: RecyclerView
    private lateinit var etChatMessage: EditText
    private lateinit var btnSendChat: View
    private val messagesList = mutableListOf<MessageModel>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        peerId = intent.getStringExtra("PEER_ID") ?: ""
        peerName = intent.getStringExtra("PEER_NAME") ?: "User"

        findViewById<TextView>(R.id.tvChatPeerName).text = peerName
        findViewById<View>(R.id.btnChatBack).setOnClickListener { finish() }

        chatRoomId = if (currentUserId < peerId) "${currentUserId}_${peerId}" else "${peerId}_${currentUserId}"

        rvMessages = findViewById(R.id.rvMessages)
        etChatMessage = findViewById(R.id.etChatMessage)
        btnSendChat = findViewById(R.id.btnSendChat)

        rvMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        adapter = MessageAdapter(messagesList, currentUserId)
        rvMessages.adapter = adapter

        listenForMessages()

        btnSendChat.setOnClickListener { sendMessage() }
    }

    private fun listenForMessages() {
        database.getReference("chats").child(chatRoomId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messagesList.clear()
                    for (msgSnapshot in snapshot.children) {
                        val msg = msgSnapshot.getValue(MessageModel::class.java)
                        if (msg != null) messagesList.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    if (messagesList.isNotEmpty()) {
                        rvMessages.smoothScrollToPosition(messagesList.size - 1)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun sendMessage() {
        val text = etChatMessage.text.toString().trim()
        if (text.isEmpty()) return

        val msgRef = database.getReference("chats").child(chatRoomId).child("messages").push()
        val msgId = msgRef.key ?: return
        val timestamp = System.currentTimeMillis()

        val msg = MessageModel(msgId, currentUserId, text, timestamp)
        msgRef.setValue(msg).addOnSuccessListener {
            etChatMessage.text.clear()

            database.getReference("users").child(currentUserId).child("name")
                .get().addOnSuccessListener { snapshot ->
                    val myName = snapshot.value?.toString() ?: "Student"

                    val myThread = hashMapOf("peerId" to peerId, "peerName" to peerName, "lastMessage" to text, "timestamp" to timestamp)
                    val peerThread = hashMapOf("peerId" to currentUserId, "peerName" to myName, "lastMessage" to text, "timestamp" to timestamp)

                    database.getReference("chat_lists").child(currentUserId).child(peerId).setValue(myThread)
                    database.getReference("chat_lists").child(peerId).child(currentUserId).setValue(peerThread)
                }
        }
    }

    inner class MessageAdapter(private val list: List<MessageModel>, private val myUid: String) :
        RecyclerView.Adapter<MessageAdapter.Holder>() {

        inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
            val root = v as LinearLayout
            val bubble = v.findViewById<MaterialCardView>(R.id.cardMsgBubble)
            val txt = v.findViewById<TextView>(R.id.tvMsgText)
        }

        override fun onCreateViewHolder(p: ViewGroup, t: Int) =
            Holder(LayoutInflater.from(p.context).inflate(R.layout.item_message_bubble, p, false))

        override fun onBindViewHolder(h: Holder, pos: Int) {
            val m = list[pos]
            h.txt.text = m.messageText

            val layoutParams = h.bubble.layoutParams as LinearLayout.LayoutParams
            if (m.senderId == myUid) {
                h.root.gravity = Gravity.END
                h.bubble.setCardBackgroundColor(Color.parseColor("#6B8DD6"))
                h.txt.setTextColor(Color.WHITE)
                layoutParams.setMargins(50, 0, 0, 0)
            } else {
                h.root.gravity = Gravity.START
                h.bubble.setCardBackgroundColor(Color.parseColor("#EEEEEE"))
                h.txt.setTextColor(Color.parseColor("#1A1A2E"))
                layoutParams.setMargins(0, 0, 50, 0)
            }
            h.bubble.layoutParams = layoutParams
        }
        override fun getItemCount() = list.size
    }
}