package com.huseyinemreseyrek.courseassistantapp


import android.R
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAttendanceInstructorBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

const val ATTENDANCE_PERMISSION_THRESHOLD_MINUTES = 10
class AttendanceInstructor : AppCompatActivity() {
    private lateinit var linearLayout: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityAttendanceInstructorBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: String
    private lateinit var user: Person
    private lateinit var currCourseNames: ArrayList<String>
    private lateinit var currCourseID: ArrayList<String>
    private lateinit var currCourseGroup: ArrayList<String>
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
        currCourseID = ArrayList()
        currCourseGroup = ArrayList()
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
                            currCourseID.add(courseDetails["courseID"] as String)
                            currCourseGroup.add(courseDetails["group"] as String)
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

        setView("first")
    }

    @SuppressLint("NewApi")
    fun setView(filter: String) {
        linearLayout = binding.linearLayout
        var index_of_list: Int = -1
        attendanceInfo = ArrayList()
        db.collection("Attendance")
            .document(userEmail)
            .collection("Attendances")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    doToast("Listen failed")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val attendanceData = document.data
                        val courseName = attendanceData?.get("courseName") as? String
                        val courseGroup = attendanceData?.get("group") as? String
                        val studentID = attendanceData?.get("studentID") as? String
                        val date = attendanceData?.get("date") as? String
                        val time = attendanceData?.get("time") as? String
                        attendanceInfo.add("$courseName,$courseGroup,$studentID,$date,$time")
                        index_of_list++
                        index_of_list++
                        val currentDateTime = LocalDateTime.now()
                        val currentDate = currentDateTime.toString().substring(0,10)
                        val daysBetween = ChronoUnit.DAYS.between(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        if(daysBetween > 6){
                            document.reference.delete()
                        }
                        else{
                            if (filter == "none" || "$courseName" == filter) {
                                val textView = TextView(this).apply {
                                    setTextColor(Color.WHITE)
                                    "$courseName, $courseGroup, $studentID, $date, $time".also { text = it }
                                    setPadding(16, 16, 16, 16)
                                }
                                linearLayout.addView(textView)
                            }
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
    }
    @SuppressLint("NewApi")
    fun startAttendance(view: View){
        val courseName: String = binding.courseNameEditText.text.toString()
        val i: Int = currCourseNames.indexOf(courseName)
        val group = currCourseGroup[i]
        val currentDateTime = LocalDateTime.now()
        val time = currentDateTime.toString().substring(11,19)
        val permissionData = hashMapOf(
            "courseName" to courseName,
            "time" to time,
            "group" to group
        )
        if(group == "Founder"){
            val founderMail = userEmail
            CoroutineScope(Dispatchers.Main).launch {
                val isSuccessful = send(founderMail,permissionData)
                if (!isSuccessful) {
                    doToast("Unsuccessful")
                }
            }
            doToast("Attendance Started")
        }
        else{
            CoroutineScope(Dispatchers.Main).launch {
                val founderMail = getFounderMail(currCourseID[i])
                CoroutineScope(Dispatchers.Main).launch {
                    val isSuccessful = send(founderMail,permissionData)
                    if (!isSuccessful) {
                        doToast("Unsuccessful")
                    }
                }
                doToast("Attendance Started")
            }
        }
    }
    suspend fun getFounderMail(courseID: String): String {
        val db = FirebaseFirestore.getInstance()
        return suspendCancellableCoroutine { continuation ->
            val docRef = db.collection("Courses").document(courseID)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        continuation.resume(document.getString("FounderInstructor").toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Document", "get failed with ", exception)
                }
        }
    }
    suspend fun send(founderMail:String, data: Map<String, Any>): Boolean {
        val db = FirebaseFirestore.getInstance()
        return suspendCancellableCoroutine { continuation ->
            val attendancePermissionCollection = db.collection("AttendancePermissions").document(founderMail).collection("Permissions")
            attendancePermissionCollection.add(data)
                .addOnSuccessListener {
                    continuation.resume(true)
                }
                .addOnFailureListener { e ->
                    continuation.resume(false)
                }
        }
    }
    fun stopAttendance(view: View) {
        val courseName: String = binding.courseNameEditText.text.toString()
        val i: Int = currCourseNames.indexOf(courseName)
        val group = currCourseGroup[i]
        if(group == "Founder"){
            val founderMail = userEmail
            val permissionCollectionRef = db.collection("AttendancePermissions").document(founderMail).collection("Permissions")
            permissionCollectionRef
                .whereEqualTo("courseName", courseName)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        for (document in documents) {
                            document.reference.delete()
                            doToast("Attendance stopped")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    doToast("Error: ${e.message}")
                }
        }
        else{
            CoroutineScope(Dispatchers.Main).launch {
                val founderMail = getFounderMail(currCourseID[i])
                val permissionCollectionRef = db.collection("AttendancePermissions").document(founderMail).collection("Permissions")
                permissionCollectionRef
                    .whereEqualTo("courseName", courseName)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents != null && !documents.isEmpty) {
                            for (document in documents) {
                                val docGroup = document.getString("group")
                                if(docGroup == group){
                                    document.reference.delete()
                                    doToast("Attendance stopped")
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        doToast("Error: ${e.message}")
                    }
            }
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
            val parts = info.split(",")
            if (parts.size == 5) {
                "${parts[1]},${parts[2]}"
                csvData.append("${parts[1]},${parts[2]},${parts[3]},${parts[4]}").append("\n")
            }
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
            doToast(fileName + "Download unsuccessful")
        }
    }
    fun doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun popUpMessage(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Okay") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}