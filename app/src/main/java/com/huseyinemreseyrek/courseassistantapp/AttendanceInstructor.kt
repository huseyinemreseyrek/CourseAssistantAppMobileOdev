package com.huseyinemreseyrek.courseassistantapp

//filter.png
import android.R
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.Manifest
import android.annotation.SuppressLint
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAttendanceInstructorBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.time.LocalDateTime


class AttendanceInstructor : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityAttendanceInstructorBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: String
    private lateinit var user: Person
    private lateinit var currCourseNames: ArrayList<String>
    private lateinit var attendanceInfo: ArrayList<String>
    private var index: Int = 0
    private var fileName: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceInstructorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Ekrana Toast mesajları ekleyerek hata ayıklama
        //doToast("setContentView called")

        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail") ?: run {
            doToast("No userEmail found in Intent")
            return
        }

        //doToast("intent received with userEmail: $userEmail")
        auth = Firebase.auth

        //doToast("FirebaseAuth initialized")

        getData()

        currCourseNames = ArrayList()
        db.collection("Instructors").document(userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ContentValues.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val instructorCourses =
                        snapshot.data?.get("RegisteredCourses") as? Map<String, Map<String, Any>>
                    if (instructorCourses != null) {
                        for ((courseName, courseDetails) in instructorCourses) {
                            currCourseNames.add(courseDetails["courseName"] as String)
                            index++
                        }
                    }
                }
            }
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, currCourseNames)
        binding.courseNameListView.adapter = adapter

        binding.courseNameListView.setOnItemClickListener { parent, view, position, id ->
            val selectedCourseName = parent.getItemAtPosition(position).toString()
            binding.courseNameEditText.setText(selectedCourseName)
            binding.courseNameListView.visibility = View.GONE
        }

        setView("none")

    }

    fun setView(filter: String) {
        linearLayout = binding.linearLayout
        var index_of_list: Int = 0
        attendanceInfo = ArrayList()
        db.collection("Attendance")
            .document(userEmail)
            .collection("Attendances")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Listen failed: $e")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val attendanceData = document.data
                        val courseName = attendanceData?.get("courseName") as? String
                        val studentID = attendanceData?.get("studentID") as? String
                        val date = attendanceData?.get("date") as? String
                        val time = attendanceData?.get("time") as? String
                        attendanceInfo.add(courseName + "," + studentID + "," + date + "," + time)
                        index_of_list++
                        val cardView = MaterialCardView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(16, 16, 16, 16)
                            }
                            radius = 8f
                            cardElevation = 4f
                        }
                        if (filter == "none" || "$courseName" == filter) {
                            val textView = TextView(this).apply {
                                doToast("found" + courseName.toString())
                                setTextColor(Color.WHITE)
                                text = attendanceInfo[index_of_list-1]
                                setPadding(16, 16, 16, 16)
                            }
                            cardView.addView(textView)
                            linearLayout.addView(cardView)
                        }
                    }
                }
            }
    }

    fun getData() {
        println("Getting data for $userEmail")
        val docRef = db.collection("Instructors").document(userEmail)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val name = documentSnapshot.getString("name") ?: "N/A"
                val surname = documentSnapshot.getString("surname") ?: "N/A"
                val email = documentSnapshot.getString("email") ?: userEmail
                val accountType = documentSnapshot.getString("accountType") ?: "N/A"
                val downloadUrl = documentSnapshot.getString("downloadUrl") ?: ""
                val educationalInfo = documentSnapshot.getString("educationalInfo") ?: ""
                val phoneNumber = documentSnapshot.getString("phoneNumber") ?: ""
                val instagramAddress = documentSnapshot.getString("instagramAddress") ?: ""
                val twitterAddress = documentSnapshot.getString("twitterAddress") ?: ""
                val currentCourses =
                    documentSnapshot.get("RegisteredCourses") as? MutableList<Map<String, Any>>
                        ?: mutableListOf()
                user = Instructor(accountType, name, surname, email, downloadUrl, educationalInfo, phoneNumber, instagramAddress, twitterAddress, currentCourses
                )
            } else {
                println("Document does not exist.")
            }
        }.addOnFailureListener { e ->
            println("Error fetching document: ${e.localizedMessage}")
        }
    }

    fun makeListVisible(view: View) {
        binding.courseNameListView.visibility = View.VISIBLE
    }

    fun filterListView(view: View) {
        var courseName: String = binding.courseNameEditText.text.toString()
        linearLayout.removeAllViews()
        setView(courseName)
    }
    @SuppressLint("NewApi")
    fun prepCSV(view: View){
        val currentDateTime = LocalDateTime.now()
        val date = currentDateTime.toString().substring(0,10)
        fileName = binding.courseNameEditText.text.toString() + "_" + date + ".csv"
        if(checkAndRequestStoragePermissions()){
            saveAttendanceInfoToCsv()
        }
        else{
            doToast("Please allow read and write external storage")
        }
    }
    private fun checkAndRequestStoragePermissions(): Boolean {
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val listPermissionsNeeded = ArrayList<String>()

        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 3)
            return false
        }
        return true
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 3){
            saveAttendanceInfoToCsv()
        }
    }
    private fun saveAttendanceInfoToCsv() {
        val csvData = StringBuilder()
        for (info in attendanceInfo) {
            doToast(info)
            csvData.append(info).append("\n")
        }

        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + fileName

        try {
            val file = File(filePath)
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(csvData.toString().toByteArray())
            fileOutputStream.close()
            doToast("$fileName downloaded successfully")
        } catch (e: IOException) {
            e.printStackTrace()
            doToast(fileName + "Download unsuccessfully")
        }
    }
    fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}