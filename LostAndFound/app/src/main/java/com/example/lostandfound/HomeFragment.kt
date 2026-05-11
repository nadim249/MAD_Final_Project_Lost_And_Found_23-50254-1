package com.example.lostandfound

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.card.MaterialCardView
import androidx.core.graphics.toColorInt


class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val avatarCard = view.findViewById<View>(R.id.avatarCard)

        avatarCard.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        val foundCard = view.findViewById<View>(R.id.card2)

        foundCard.setOnClickListener {
            // Example: Change color or navigate
            val intent = Intent(requireContext(), ItemDetailsActivity::class.java)
            startActivity(intent)
        }

    }


}