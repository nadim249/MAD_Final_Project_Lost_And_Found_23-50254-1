package com.example.lostandfound

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.Calendar

class PostFragment : Fragment() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // Form Selection States
    private var selectedType = "Lost"
    private var selectedCategory = "Electronics"
    private var selectedDateString = ""
    private var selectedImageUri: Uri? = null

    // UI Element Hooks
    private lateinit var cardLost: MaterialCardView
    private lateinit var cardFound: MaterialCardView
    private lateinit var etItemTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var tvDate: TextView
    private lateinit var cardDate: MaterialCardView
    private lateinit var cardAddPhoto: MaterialCardView
    private lateinit var photoThumb1: ConstraintLayout
    private lateinit var btnRemovePhoto1: MaterialCardView
    private lateinit var ivPhoto1: ImageView
    private lateinit var btnPostCard: MaterialCardView
    private lateinit var btnPostItem: TextView

    // Category Tags UI Hooks
    private lateinit var tagElectronics: MaterialCardView
    private lateinit var tagClothing: MaterialCardView
    private lateinit var tagBooks: MaterialCardView
    private lateinit var tagKeys: MaterialCardView
    private lateinit var tagIdCard: MaterialCardView
    private lateinit var tagAccessories: MaterialCardView
    private lateinit var tagSports: MaterialCardView

    // Modern Photo Picker Setup
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->        if (uri != null) {
            try {
                selectedImageUri = uri
                photoThumb1.visibility = View.VISIBLE
                cardAddPhoto.visibility = View.GONE
                ivPhoto1.setImageURI(uri) // Show preview in the thumbnail box
            } catch (e: Exception) {
                Log.e("PostFragment", "Error setting image preview", e)
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_post, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        initializeViews(view)
        setupClickListeners()

        // Initialize default UI states
        resetCategoryTagsUi()
        applyCategoryTagSelection(tagElectronics, "Electronics")
    }

    private fun initializeViews(view: View) {
        cardLost = view.findViewById(R.id.cardLost)
        cardFound = view.findViewById(R.id.cardFound)
        etItemTitle = view.findViewById(R.id.etItemTitle)
        etDescription = view.findViewById(R.id.etDescription)
        etLocation = view.findViewById(R.id.etLocation)
        tvDate = view.findViewById(R.id.tvDate)
        cardDate = view.findViewById(R.id.cardDate)
        cardAddPhoto = view.findViewById(R.id.cardAddPhoto)
        photoThumb1 = view.findViewById(R.id.photoThumb1)
        btnRemovePhoto1 = view.findViewById(R.id.btnRemovePhoto1)
        ivPhoto1 = view.findViewById(R.id.ivPhoto1)
        btnPostItem = view.findViewById(R.id.btnPostItem)
        btnPostCard = view.findViewById(R.id.btnPostCard)

        // Tags
        tagElectronics = view.findViewById(R.id.tagElectronics)
        tagClothing = view.findViewById(R.id.tagClothing)
        tagBooks = view.findViewById(R.id.tagBooks)
        tagKeys = view.findViewById(R.id.tagKeys)
        tagIdCard = view.findViewById(R.id.tagIdCard)
        tagAccessories = view.findViewById(R.id.tagAccessories)
        tagSports = view.findViewById(R.id.tagSports)

        // Initial Photo State
        photoThumb1.visibility = View.GONE
        cardAddPhoto.visibility = View.VISIBLE
    }

    private fun setupClickListeners() {
        // Toggle Item Status
        cardLost.setOnClickListener {
            selectedType = "Lost"
            cardLost.setStrokeColor(Color.parseColor("#E57373"))
            cardLost.setStrokeWidth(4)
            cardFound.setStrokeColor(Color.parseColor("#E0E0E0"))
            cardFound.setStrokeWidth(3)
        }

        cardFound.setOnClickListener {
            selectedType = "Found"
            cardFound.setStrokeColor(Color.parseColor("#4CAF82"))
            cardFound.setStrokeWidth(4)
            cardLost.setStrokeColor(Color.parseColor("#E0E0E0"))
            cardLost.setStrokeWidth(3)
        }

        // Calendar Date Picker
        cardDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selYear, selMonth, selDay ->
                val formattedMonth = String.format("%02d", selMonth + 1)
                val formattedDay = String.format("%02d", selDay)
                selectedDateString = "$formattedMonth/$formattedDay/$selYear"
                tvDate.text = selectedDateString
                tvDate.setTextColor(Color.parseColor("#1A1A2E"))
            }, year, month, day)
            datePickerDialog.show()
        }

        // TO THIS:
        cardAddPhoto.setOnClickListener {
            pickMedia.launch("image/*")
        }

        btnRemovePhoto1.setOnClickListener {
            selectedImageUri = null
            photoThumb1.visibility = View.GONE
            cardAddPhoto.visibility = View.VISIBLE
            ivPhoto1.setImageDrawable(null)
        }

        // Tag Option Click Bindings
        tagElectronics.setOnClickListener { applyCategoryTagSelection(tagElectronics, "Electronics") }
        tagClothing.setOnClickListener { applyCategoryTagSelection(tagClothing, "Clothing") }
        tagBooks.setOnClickListener { applyCategoryTagSelection(tagBooks, "Books") }
        tagKeys.setOnClickListener { applyCategoryTagSelection(tagKeys, "Keys") }
        tagIdCard.setOnClickListener { applyCategoryTagSelection(tagIdCard, "ID Card") }
        tagAccessories.setOnClickListener { applyCategoryTagSelection(tagAccessories, "Accessories") }
        tagSports.setOnClickListener { applyCategoryTagSelection(tagSports, "Sports") }

        // Form Submit Execution
        btnPostCard.setOnClickListener {
            executeFormSubmission()
        }
    }

    private fun applyCategoryTagSelection(selectedCard: MaterialCardView, categoryLabel: String) {
        resetCategoryTagsUi()
        selectedCategory = categoryLabel
        selectedCard.setStrokeColor(Color.parseColor("#6B8DD6"))
        selectedCard.setStrokeWidth(4)

        val innerContainer = selectedCard.getChildAt(0) as? ViewGroup
        val labelTextView = innerContainer?.getChildAt(1) as? TextView
        labelTextView?.setTypeface(null, android.graphics.Typeface.BOLD)
        labelTextView?.setTextColor(Color.parseColor("#6B8DD6"))
    }

    private fun resetCategoryTagsUi() {
        val tagsList = listOf(tagElectronics, tagClothing, tagBooks, tagKeys, tagIdCard, tagAccessories, tagSports)
        for (card in tagsList) {
            card.setStrokeColor(Color.parseColor("#DDDDDD"))
            card.setStrokeWidth(2)
            val innerContainer = card.getChildAt(0) as? ViewGroup
            val labelTextView = innerContainer?.getChildAt(1) as? TextView
            labelTextView?.setTypeface(null, android.graphics.Typeface.NORMAL)
            labelTextView?.setTextColor(Color.parseColor("#555555"))
        }
    }

    private fun executeFormSubmission() {
        try {
            val title = etItemTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val currentUserId = auth.currentUser?.uid

            if (title.isEmpty() || description.isEmpty() || location.isEmpty() || selectedDateString.isEmpty()) {
                Toast.makeText(requireContext(), "Please complete all mandatory text fields.", Toast.LENGTH_SHORT).show()
                return
            }

            if (currentUserId == null) {
                Toast.makeText(requireContext(), "User session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                return
            }

            // Show loading state
            btnPostItem.text = "Saving..."

            val itemRef = database.getReference("items").push()

            // --- BASE64 CONVERSION FLOW ---
            var base64ImageString = "bell" // Fallback to our dummy icon name

            if (selectedImageUri != null) {
                try {
                    base64ImageString = encodeImageToBase64(selectedImageUri!!)
                } catch (e: Exception) {
                    Log.e("PostFragment", "Failed to process image.", e)
                    Toast.makeText(requireContext(), "Failed to process image, saving without it.", Toast.LENGTH_SHORT).show()
                }
            }

            // Save directly to Realtime Database
            saveItemToDatabase(itemRef, title, description, location, currentUserId, base64ImageString)

        } catch (e: Exception) {
            Log.e("PostFragment", "Crash captured during submission", e)
            btnPostItem.text = "Post Item"
            Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show()
        }
    }

    // Shrinks the image and turns it into a Base64 Text String
    private fun encodeImageToBase64(uri: Uri): String {
        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: throw Exception("Failed to decode bitmap")

            // 1. Shrink the image to a max width of 600px to save database space
            val maxWidth = 600
            val scale = maxWidth.toFloat() / originalBitmap.width
            val newHeight = (originalBitmap.height * scale).toInt()
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, maxWidth, newHeight, true)

            // 2. Compress the image to JPEG at 60% quality
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)

            // 3. Convert to Base64 String
            val imageBytes = outputStream.toByteArray()
            return Base64.encodeToString(imageBytes, Base64.DEFAULT)
        }
        throw Exception("Failed to open input stream")
    }

    private fun saveItemToDatabase(itemRef: DatabaseReference, title: String, description: String, location: String, uid: String, imageString: String) {
        val itemPostDataStructure = hashMapOf(
            "title" to title,
            "description" to description,
            "type" to selectedType,
            "location" to location,
            "date" to selectedDateString,
            "postedBy" to uid,
            "status" to "Active",
            "imagePath" to imageString, // Holds Base64 TEXT or "bell" fallback
            "color" to "Not specified",
            "brand" to selectedCategory,
            "size" to "Standard"
        )

        itemRef.setValue(itemPostDataStructure)
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    Toast.makeText(requireContext(), "Item posted successfully!", Toast.LENGTH_SHORT).show()

                    // Reset Form
                    etItemTitle.text.clear()
                    etDescription.text.clear()
                    etLocation.text.clear()
                    tvDate.text = "mm/dd/yyyy"
                    tvDate.setTextColor(Color.parseColor("#AAAAAA"))
                    selectedDateString = ""
                    selectedImageUri = null
                    photoThumb1.visibility = View.GONE
                    cardAddPhoto.visibility = View.VISIBLE
                    ivPhoto1.setImageDrawable(null)

                    // Go back to Home tab
                    activity?.findViewById<View>(R.id.nav_home)?.performClick()
                } else {
                    Toast.makeText(requireContext(), "Database failed: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
                btnPostItem.text = "Post Item"
            }
    }
}