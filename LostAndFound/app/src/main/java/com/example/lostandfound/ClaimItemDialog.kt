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
import android.util.Log
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class ClaimItemDialog : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    companion object {
        const val TAG = "ClaimItemDialog"
        fun newInstance() = ClaimItemDialog()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_claim_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val etMessage = view.findViewById<TextInputEditText>(R.id.etClaimVerificationMessage)
        val btnSubmit = view.findViewById<View>(R.id.btnSubmitClaim)
        val tvButtonText = view.findViewById<TextView>(R.id.tvSubmitClaimButtonText)

        val hostActivity = activity as? ItemDetailsActivity
        val currentItemField = hostActivity?.let {
            try {
                val field = it.javaClass.getDeclaredField("item")
                field.isAccessible = true
                field.get(it) as? ItemModel
            } catch (e: Exception) {
                Log.e(TAG, "Reflective item recovery crash", e)
                null
            }
        }

        if (currentItemField == null) {
            Toast.makeText(context, "Error tracking item context data.", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        btnSubmit.setOnClickListener {
            try {
                val userVerificationInput = etMessage.text.toString().trim()
                val currentUserId = auth.currentUser?.uid ?: return@setOnClickListener

                if (userVerificationInput.isEmpty()) {
                    Toast.makeText(context, "Please enter proof descriptions to submit.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                tvButtonText.text = "Submitting..."
                btnSubmit.isEnabled = false

                database.getReference("users").child(currentUserId).child("name")
                    .get().addOnSuccessListener { userSnapshot ->
                        val currentStudentName = userSnapshot.value?.toString() ?: "Campus Student"
                        val timestamp = System.currentTimeMillis()

                        val claimRef = database.getReference("claims").push()
                        val generatedClaimId = claimRef.key ?: return@addOnSuccessListener

                        // Assemble structural object
                        val claimData = ClaimModel(
                            claimId = generatedClaimId,
                            itemId = currentItemField.id,
                            itemTitle = currentItemField.title,
                            claimedBy = currentUserId,
                            claimantName = currentStudentName,
                            itemOwnerId = currentItemField.postedBy,
                            message = userVerificationInput,
                            timestamp = timestamp,
                            status = "Pending"
                        )

                        claimRef.setValue(claimData).addOnCompleteListener { claimTask ->
                            if (claimTask.isSuccessful) {

                                val notificationPayload = hashMapOf(
                                    "title" to "New Claim Submitted!",
                                    "body" to "$currentStudentName claims ownership over '${currentItemField.title}'",
                                    "timestamp" to timestamp,
                                    "type" to "ClaimRequest",
                                    "referenceId" to generatedClaimId
                                )
                                database.getReference("notifications").child(currentItemField.postedBy).push().setValue(notificationPayload)

                                val statMetricNode = if (currentItemField.type == "Lost") "itemsLost" else "itemsFound"
                                database.getReference("users").child(currentUserId).child("stats").child(statMetricNode)
                                    .setValue(ServerValue.increment(1))

                                Toast.makeText(context, "Claim request submitted to office!", Toast.LENGTH_SHORT).show()
                                dismiss()
                            } else {
                                Toast.makeText(context, "Submission failed. Try again.", Toast.LENGTH_SHORT).show()
                                tvButtonText.text = "Submit Claim Request"
                                btnSubmit.isEnabled = true
                            }
                        }
                    }.addOnFailureListener {
                        tvButtonText.text = "Submit Claim Request"
                        btnSubmit.isEnabled = true
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Crash captured during structural transaction execution steps", e)
            }
        }
    }
}