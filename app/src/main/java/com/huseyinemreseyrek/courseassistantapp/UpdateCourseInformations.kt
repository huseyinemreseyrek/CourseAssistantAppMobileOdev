package com.huseyinemreseyrek.courseassistantapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityCourseInformationsBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityUpdateCourseInformationsBinding

class UpdateCourseInformations : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateCourseInformationsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var userEmail : String
    private lateinit var courseID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateCourseInformationsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail").toString()
        courseID = intent.getStringExtra("courseID").toString()
    }
}