package com.huseyinemreseyrek.courseassistantapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAssignStudentsBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityCourseInformationsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AssignStudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignStudentsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var userEmail : String
    private lateinit var courseID : String
    private lateinit var groupNumber : String
    private lateinit var mainInstructor : String
    private lateinit var courseName : String
    private lateinit var status : String
    private lateinit var date : Timestamp
    private var REQUEST_CODE = 100
    private val list = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAssignStudentsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail").toString()
        courseID = intent.getStringExtra("courseID").toString()
        groupNumber = intent.getStringExtra("groupNumber").toString()
        mainInstructor = intent.getStringExtra("mainInstructor").toString()
        courseName = intent.getStringExtra("courseName").toString()
        status = intent.getStringExtra("status").toString()
        val timestampSeconds = intent.getLongExtra("date",-1)
        if (timestampSeconds != -1L) {
            date = Timestamp(timestampSeconds, 0)
            Log.d("TargetActivity", "Received Timestamp: ${date.toDate()}")
        } else {
            Log.d("TargetActivity", "No Timestamp received")
        }


    }

    fun automaticAssign(view : View){
        selectCsvFile()
    }
    private fun selectCsvFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val fileName = getFileName(uri)
                if (fileName.endsWith(".csv")) {
                    try {
                        list.clear()
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                                var line: String?

                                reader.readLine()
                                while (reader.readLine().also { line = it } != null) {
                                    val tokens = line!!.split(',')
                                    if (tokens.size >= 2) {
                                        list.add(Pair(tokens[0], tokens[1]))
                                    }
                                }

                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Dosya okunurken bir hata oldu, lütfen geçerli bir CSV dosyası seçin.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Lütfen bir CSV dosyası secin.", Toast.LENGTH_LONG).show()
                }
            }
        }
        println(list)
        writeStudentsToCourses()


    }

    private fun writeStudentsToCourses(){
        val groupNum = "Group$groupNumber"
        GlobalScope.launch {
            list.forEach { pair ->
                val tempUserEmail = pair.second
                val documentData = hashMapOf("email" to tempUserEmail)

                try {
                    withContext(Dispatchers.IO) {
                        val docRef = db.collection("Courses").document(courseID)
                            .collection(groupNum).document(tempUserEmail)
                        docRef.set(documentData).await()
                        Log.d("TAG", "DocumentSnapshot successfully written!")
                    }

                    withContext(Dispatchers.IO) {
                        val collectionPath = if(tempUserEmail.endsWith("@std.yildiz.edu.tr")) "Students" else "Instructors"
                        val newCourse = mapOf(
                            courseID to mapOf(
                                "courseName" to courseName,
                                "mainInstructor" to mainInstructor,
                                "courseID" to courseID,
                                "group" to groupNumber,
                                "status" to status,
                                "date" to date
                            )
                        )
                        db.collection(collectionPath).document(tempUserEmail)
                            .set(mapOf("RegisteredCourses" to newCourse), SetOptions.merge()).await()
                        println("New Students added successfully!")
                    }
                } catch (e: Exception) {
                    Log.w("TAG", "Error in transaction: ${e.message}")
                }
            }
        }
        Toast.makeText(this, "Students Assigned", Toast.LENGTH_SHORT).show()
    }

    private fun getFileName(uri: Uri): String {
        var name = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex >= 0) {
                    name = it.getString(columnIndex)
                }
            }
        }
        return name
    }

    fun manualAssign(view : View){
        val email = binding.emailTextView.text.toString()
        val groupNum = "Group$groupNumber"
        if(email.endsWith("@std.yildiz.edu.tr")){
            GlobalScope.launch {
                val documentData = hashMapOf("email" to email)

                try {
                    withContext(Dispatchers.IO) {
                        val docRef = db.collection("Courses").document(courseID)
                            .collection(groupNum).document(email)
                        docRef.set(documentData).await()
                        Log.d("TAG", "DocumentSnapshot successfully written!")
                    }

                    withContext(Dispatchers.IO) {
                        val newCourse = mapOf(
                            courseID to mapOf(
                                "courseName" to courseName,
                                "mainInstructor" to mainInstructor,
                                "courseID" to courseID,
                                "group" to groupNumber,
                                "status" to status,
                                "date" to date
                            )
                        )
                        db.collection("Students").document(email)
                            .set(mapOf("RegisteredCourses" to newCourse), SetOptions.merge()).await()
                        println("New Students added successfully!")
                    }
                } catch (e: Exception) {
                    Log.w("TAG", "Error in transaction: ${e.message}")
                }
            }
            Toast.makeText(this, "Students Assigned", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
        }

    }



}