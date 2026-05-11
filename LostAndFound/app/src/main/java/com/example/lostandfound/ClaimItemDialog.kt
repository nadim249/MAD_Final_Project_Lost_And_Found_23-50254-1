package com.example.lostandfound

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class ClaimItemDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_claim_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose      = view.findViewById<View>(R.id.btnClose)
        val btnCancel     = view.findViewById<View>(R.id.btnCancel)
        val btnSubmit     = view.findViewById<View>(R.id.btnSubmit)
        val etVerification  = view.findViewById<EditText>(R.id.etVerificationDetails)
        val etMeetingLocation = view.findViewById<EditText>(R.id.etMeetingLocation)
        val etContactMethod   = view.findViewById<EditText>(R.id.etContactMethod)

        // Close / Cancel → dismiss
        btnClose.setOnClickListener { dismiss() }
        btnCancel.setOnClickListener { dismiss() }

        // Submit
        btnSubmit.setOnClickListener {
            val verification = etVerification.text.toString().trim()
            val location     = etMeetingLocation.text.toString().trim()
            val contact      = etContactMethod.text.toString().trim()

            if (verification.isEmpty()) {
                etVerification.error = "Please describe the item"
                return@setOnClickListener
            }
            if (contact.isEmpty()) {
                etContactMethod.error = "Please provide a contact method"
                return@setOnClickListener
            }

            // TODO: send claim to backend
            Toast.makeText(requireContext(), "Claim submitted successfully!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // Make dialog full width with margin
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    companion object {
        const val TAG = "ClaimItemDialog"
        fun newInstance() = ClaimItemDialog()
    }
}