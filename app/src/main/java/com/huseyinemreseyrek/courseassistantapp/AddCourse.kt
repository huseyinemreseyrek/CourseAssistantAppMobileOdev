package com.huseyinemreseyrek.courseassistantapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAccountInformationBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAddCourseBinding

class AddCourse : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityAddCourseBinding
    private var receivedList = ArrayList<Pair<String, String>>()
    private var courseID = ""
    private var groupNumber = 0
    private lateinit var userEmail : String
    private lateinit var courseDate : Timestamp

    override fun onCreate(savedInstanceState: Bundle?) {
        println("AddCourseControl-1")
        super.onCreate(savedInstanceState)
        binding = ActivityAddCourseBinding.inflate(layoutInflater)
        val view = binding.root
        println("AddCourseControl0")
        setContentView(view)
        db = Firebase.firestore
        userEmail = intent.getStringExtra("email").toString()
        println("AddCourseControl1")
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) { receivedList = (data.getSerializableExtra("instructors") as? ArrayList<Pair<String, String>>)!!
                    }
                    createGroupsSaveInstructors()

                }
            }
    }

    private fun checkInfos(): Boolean {
        println("AddCourseControl2")
        val courseID = binding.courseIDEditText.text.toString()
        val courseName = binding.courseNameEditText.text.toString()
        val groupNumbers = binding.groupNumbersEditText.text.toString()
        if (courseID.isEmpty()) {
            Toast.makeText(this, "Enter courseID", Toast.LENGTH_SHORT).show()
            return false
        } else if (courseName.isEmpty()) {
            Toast.makeText(this, "Enter courseName", Toast.LENGTH_SHORT).show()
            return false
        } else if (groupNumbers.isEmpty()) {
            Toast.makeText(this, "enter groupNumbers", Toast.LENGTH_SHORT).show()
            return false
        } else if (groupNumbers.toInt() > 10) {
            Toast.makeText(this, "Group Numbers should be less than 10", Toast.LENGTH_SHORT).show()
            return false
        }
        return true

    }


    fun addCourse(view: View) {

        if (checkInfos()) {
            courseID = binding.courseIDEditText.text.toString()
            groupNumber = binding.groupNumbersEditText.text.toString().toInt()
            val courseMap = hashMapOf<String, Any>()
            courseMap["CourseID"] = courseID
            courseMap["CourseName"] = binding.courseNameEditText.text.toString()
            courseMap["GroupNumbers"] = binding.groupNumbersEditText.text.toString()
            courseDate = Timestamp.now()
            courseMap["Date"] = courseDate
            courseMap["FounderInstructor"] = userEmail
            db.collection("Courses").document(binding.courseIDEditText.text.toString())
                .set(courseMap).addOnSuccessListener {
                Toast.makeText(this, "Course added!", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {
                Toast.makeText(this@AddCourse, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
            val intent = Intent(this, AddInstructorToCourse::class.java)
            intent.putExtra("groupNumber", binding.groupNumbersEditText.text.toString())
            intent.putExtra("userEmail",userEmail)
            resultLauncher.launch(intent)
        }
    }

    private fun createGroupsSaveInstructors() {
        val courseDocument = db.collection("Courses").document(courseID)
        val totalDocuments = receivedList.size
        val tasks = mutableListOf<Task<*>>()

        for (pair in receivedList) {
            val documentName = pair.first
            val groupName = "Group${pair.second}"
            val groupCollection = courseDocument.collection(groupName)
            val customDocument = groupCollection.document(documentName)
            val emailData = hashMapOf("email" to pair.first)

            val newCourse = mapOf(
                courseID to mapOf(
                    "courseName" to binding.courseNameEditText.text.toString(),
                    "mainInstructor" to userEmail,
                    "courseID" to courseID,
                    "group" to pair.second,
                    "status" to "Attending",
                    "date" to courseDate
                )
            )

            val instructorTask = db.collection("Instructors").document(pair.first)
                .set(mapOf("RegisteredCourses" to newCourse), SetOptions.merge())

            val documentTask = customDocument.set(emailData)

            tasks.add(instructorTask)
            tasks.add(documentTask)
        }

        Tasks.whenAllSuccess<Any>(tasks).addOnSuccessListener {
            println("All tasks completed successfully")
            val newCourse = mapOf(
                courseID to mapOf(
                    "courseName" to binding.courseNameEditText.text.toString(),
                    "mainInstructor" to userEmail,
                    "courseID" to courseID,
                    "group" to "Founder",
                    "status" to "Attending",
                    "date" to courseDate
                )
            )
            db.collection("Instructors").document(userEmail)
                .set(mapOf("RegisteredCourses" to newCourse), SetOptions.merge())

            finish()
        }.addOnFailureListener { e ->
            println("An error occurred with one of the tasks: ${e.message}")
        }
    }

}

