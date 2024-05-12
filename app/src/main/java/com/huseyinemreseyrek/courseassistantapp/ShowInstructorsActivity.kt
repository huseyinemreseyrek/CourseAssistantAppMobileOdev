package com.huseyinemreseyrek.courseassistantapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityCourseInformationsBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityShowInstructorsBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityUpdateCourseInformationsBinding

class ShowInstructorsActivity : AppCompatActivity() {
    private lateinit var recyclerViewShowInstructorAdapter: RecyclerViewShowInstructorAdapter
    private lateinit var instructorsList : ArrayList<ShowInstructorDataClass>
    private lateinit var binding: ActivityShowInstructorsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var userEmail : String
    private lateinit var courseID : String
    private lateinit var totalGroupNumbers : String
    private lateinit var mainInstructor : String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowInstructorsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail").toString()
        courseID = intent.getStringExtra("courseID").toString()
        mainInstructor = intent.getStringExtra("mainInstructor").toString()
        totalGroupNumbers = intent.getStringExtra("totalGroupNumbers").toString()
        setSupportActionBar(binding.myToolbar)
        instructorsList = ArrayList()

        binding.instructorsList.layoutManager = LinearLayoutManager(this)
        recyclerViewShowInstructorAdapter = RecyclerViewShowInstructorAdapter(instructorsList)
        binding.instructorsList.adapter = recyclerViewShowInstructorAdapter

        fetchAllEmails()

    }

    private fun fetchAllEmails() {
        val tasks = mutableListOf<Task<*>>()
        val person = ShowInstructorDataClass(mainInstructor,"Founder")
        instructorsList.add(person)
        for (i in 0..totalGroupNumbers.toInt()) {
            val groupName = if (i == 0) "Group" else "Group$i"
            val groupRef = db.collection("Courses").document(courseID).collection(groupName)

            val task = groupRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    snapshot.documents.forEach { document ->
                        val documentName = document.id
                        if(!documentName.endsWith("@std.yildiz.edu.tr")){
                            if(groupName == "Group"){
                                val instructor = ShowInstructorDataClass(documentName,"N/A")
                                instructorsList.add(instructor)
                            }
                            else{
                                val instructor = ShowInstructorDataClass(documentName, groupName)
                                instructorsList.add(instructor)
                            }
                        }
                    }
                } else {
                    println("No documents found in $groupName")
                }
                recyclerViewShowInstructorAdapter.notifyDataSetChanged()
            }.addOnFailureListener {
                println("Failed to fetch documents for $groupName: ${it.message}")
            }
            tasks.add(task)
        }

        Tasks.whenAll(tasks).addOnCompleteListener {
            if (it.isSuccessful) {
                println(instructorsList)
            } else {
                println("Error fetching documents: ${it.exception?.message}")
            }
        }
    }



    private fun getData() {
        TODO("Not yet implemented")
    }


}