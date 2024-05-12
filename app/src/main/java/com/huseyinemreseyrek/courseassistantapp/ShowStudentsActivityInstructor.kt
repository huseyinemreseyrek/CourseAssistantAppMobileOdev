package com.huseyinemreseyrek.courseassistantapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityShowInstructorsBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityShowStudentsInstructorBinding

class ShowStudentsActivityInstructor : AppCompatActivity() {

    private lateinit var recyclerViewShowStudentsAdapter: RecyclerViewShowStudentsAdapter
    private lateinit var studentsList : ArrayList<ShowStudentsDataClass>
    private lateinit var binding: ActivityShowStudentsInstructorBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var userEmail : String
    private lateinit var courseID : String
    private lateinit var totalGroupNumbers : String
    private lateinit var selectedGroup : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowStudentsInstructorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail").toString()
        courseID = intent.getStringExtra("courseID").toString()
        totalGroupNumbers = intent.getStringExtra("totalGroupNumbers").toString()
        selectedGroup = intent.getStringExtra("selectedGroup").toString()
        setSupportActionBar(binding.myToolbar)
        studentsList = ArrayList()
        binding.studentList.layoutManager = LinearLayoutManager(this)
        recyclerViewShowStudentsAdapter = RecyclerViewShowStudentsAdapter(studentsList)
        binding.studentList.adapter = recyclerViewShowStudentsAdapter

        getDataSelectedGroup()

    }

    private fun getDataSelectedGroup(){
        if(selectedGroup != "All Group"){
            val groupRef = db.collection("Courses").document(courseID).collection(selectedGroup)
            groupRef.get().addOnSuccessListener { snapshot ->
                if(!snapshot.isEmpty){
                    snapshot.documents.forEach { document ->
                        val documentName = document.id
                        if(documentName.endsWith("@std.yildiz.edu.tr")){
                            val student = ShowStudentsDataClass(documentName)
                            studentsList.add(student)
                        }
                    }
                }
                recyclerViewShowStudentsAdapter.notifyDataSetChanged()
            }.addOnFailureListener{
                println("Failed to fetch documents: ${it.message}")
            }
        }
        else{
            val tasks = mutableListOf<Task<*>>()
            for (i in 1..totalGroupNumbers.toInt()) {
                val groupName = "Group$i"
                val groupRef = db.collection("Courses").document(courseID).collection(groupName)
                val task = groupRef.get().addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        snapshot.documents.forEach { document ->
                            val documentName = document.id
                            if(documentName.endsWith("@std.yildiz.edu.tr")){
                                if(groupName == "Group"){
                                    val student = ShowStudentsDataClass(documentName)
                                    studentsList.add(student)
                                }
                                else{
                                    val student = ShowStudentsDataClass(documentName)
                                    studentsList.add(student)
                                }
                            }
                        }
                    } else {
                        println("No documents found in $groupName")
                    }
                    recyclerViewShowStudentsAdapter.notifyDataSetChanged()
                }.addOnFailureListener {
                    println("Failed to fetch documents for $groupName: ${it.message}")
                }
                tasks.add(task)
            }

            Tasks.whenAll(tasks).addOnCompleteListener {
                if (it.isSuccessful) {
                    println(studentsList)
                } else {
                    println("Error fetching documents: ${it.exception?.message}")
                }
            }

        }
    }

}