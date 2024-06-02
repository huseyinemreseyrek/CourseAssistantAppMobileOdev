package com.huseyinemreseyrek.courseassistantapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityClassroomBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClassroomActivity : AppCompatActivity() {
    private lateinit var recyclerViewPostAdapter : PostAdapter
    private lateinit var postList : ArrayList<Post>
    private lateinit var binding: ActivityClassroomBinding
    private lateinit var userEmail : String
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userNameSurname : String
    private lateinit var courseID : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassroomBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        setSupportActionBar(binding.toolbar11)
        auth = Firebase.auth
        userEmail = intent.getStringExtra("userEmail")!!
        courseID = intent.getStringExtra("courseID")!!
        postList = ArrayList()
        binding.postList.layoutManager = LinearLayoutManager(this)
        recyclerViewPostAdapter = PostAdapter(postList,userEmail,courseID)
        binding.postList.adapter = recyclerViewPostAdapter
        fetchUserNameSurname(userEmail)
        
        prepareData()

    }

    private fun fetchUserNameSurname(userEmail: String) {
        var collectionName = ""
        collectionName = if(userEmail.endsWith("@std.yildiz.edu.tr")){
            "Students"
        }else{
            "Instructors"
        }
        val documentReference = db.collection(collectionName).document(userEmail)
        documentReference.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val name = document.getString("name")
                val surname = document.getString("surname")
                userNameSurname = "$name $surname"
                Log.d("Firestore", "Name: $name, Surname: $surname")
            }
            else {
                Log.d("Firestore", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d("Firestore", "get failed with ", exception)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_post_menu, menu)
        if(userEmail.endsWith("@std.yildiz.edu.tr")){
            menu?.findItem(R.id.addPost)?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        showAnnouncementDialog()
        return super.onOptionsItemSelected(item)
    }


    private fun showAnnouncementDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_annoucement, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("New announcement")
            .setPositiveButton("Send") { dialog, _ ->
                val announcementText = dialogView.findViewById<EditText>(R.id.editTextAnnouncement).text.toString()
                val sendNotification = dialogView.findViewById<CheckBox>(R.id.checkBoxSendNotification).isChecked
                if(announcementText.isEmpty()){
                    Toast.makeText(this, "Please enter the annoucement", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                else{
                    sendAnnouncement(announcementText, sendNotification)
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        dialogBuilder.create().show()
    }

    private fun sendNotificationToStudents(courseID: String, announcementText: String) {

    }

    private fun sendAnnouncement(announcementText: String, sendNotification: Boolean) {
        val classroomCollection = db.collection("Courses").document(courseID).collection("Classroom")
        val announcementData = hashMapOf(
            "Date" to Date(),
            "Email" to userEmail,
            "commentnumber" to "0",
            "mainText" to announcementText,
            "nameSurname" to userNameSurname
        )

        classroomCollection.add(announcementData)
            .addOnSuccessListener { documentReference ->
                if (sendNotification) {
                    Toast.makeText(this, "Successfully announced(Notification will send)", Toast.LENGTH_SHORT).show()
                    sendNotificationToStudents(courseID, announcementText)
                }
                else {
                    Toast.makeText(this, "Succesfully announced", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "There is an error, try later", Toast.LENGTH_SHORT).show()
            }
        
    }

    private fun getFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }
    private fun prepareData() {
        val classroomCollection = db.collection("Courses").document(courseID).collection("Classroom")
        println(courseID)
        classroomCollection.orderBy("Date",
            Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if(error != null){
                Toast.makeText(this,error.localizedMessage,Toast.LENGTH_SHORT).show()
            }
            else{
                println("Classroom class control 1")
                if(value != null){
                    println("Classroom class control 2")
                    if(!value.isEmpty){
                        println("Classroom class control 3")
                        val documents = value.documents
                        postList.clear()
                        for(document in documents){
                            val commentNumber = document.getString("commentnumber")
                            val date = document.getDate("Date")
                            val email = document.getString("Email")
                            val mainText = document.getString("mainText")
                            val nameSurname = document.getString("nameSurname")
                            val stringDate = getFormattedDate(date!!)
                            val documentID = document.id
                            val post = Post(stringDate,email!!,commentNumber!!,mainText!!,nameSurname!!,documentID)
                            postList.add(post)
                            println(post)
                            println(post.getDocumentId() + " haha")
                        }
                        recyclerViewPostAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}