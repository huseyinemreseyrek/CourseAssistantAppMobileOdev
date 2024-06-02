package com.huseyinemreseyrek.courseassistantapp

import android.graphics.Rect
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityCommentsBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityInstructorCoursesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsActivity : AppCompatActivity() {
    private lateinit var recyclerViewCommentsAdapter : RecyclerViewCommentsAdapter
    private lateinit var commentList : ArrayList<CommentDataClass>
    private lateinit var binding: ActivityCommentsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: String
    private lateinit var db: FirebaseFirestore
    private lateinit var courseID : String
    private lateinit var documentId : String
    private lateinit var userNameSurname : String
    private lateinit var postNameSurname: String
    private lateinit var commentNumber : String
    private var notificationState : Boolean = false
    private lateinit var studentsList : ArrayList<String>
    private lateinit var totalGroupNumber : String
    private lateinit var notificationText : String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail")!!
        println(userEmail)
        documentId = intent.getStringExtra("postId")!!
        println("$documentId haha")
        println("DocumentID geldi mi?")
        courseID = intent.getStringExtra("courseID")!!
        println(courseID)
        postNameSurname = intent.getStringExtra("postNameSurname")!!
        commentNumber = intent.getStringExtra("postCommentNumber")!!
        totalGroupNumber = intent.getStringExtra("totalGroupNumber")!!

        println(postNameSurname)
        notificationState = intent.getBooleanExtra("notificationState",false)
        binding.main.viewTreeObserver.addOnGlobalLayoutListener {
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val r = Rect()
                    binding.main.getWindowVisibleDisplayFrame(r)
                    val screenHeight = binding.main.rootView.height
                    val keypadHeight = screenHeight - r.bottom
                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                        binding.commentSection.setPadding(0, 0, 0, keypadHeight)

                    } else {
                        binding.commentSection.setPadding(0, 0, 0, 0)
                    }

                }

            }
        }
        updatePostUI()
        fetchUserNameSurname(userEmail)
        fetchCommentNumber()

        binding.sendComment.setOnClickListener {
            saveComment()
            if(!userEmail.endsWith("@std.yildiz.edu.tr")){
                sendNotificationToStudents(intent.getStringExtra("postMainText")!!)
            }
        }

        commentList = ArrayList()
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        recyclerViewCommentsAdapter = RecyclerViewCommentsAdapter(commentList)
        binding.commentsRecyclerView.adapter = recyclerViewCommentsAdapter
        fetchComments()
        if(!userEmail.endsWith("@std.yildiz.edu.tr")){
            studentsList = ArrayList()
            fetchEmailList()
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }


    }
    private fun sendNotificationToStudents(announcementText: String) {
        val studentsCollection = Firebase.firestore.collection("Students")
        val handler = android.os.Handler(Looper.getMainLooper())
        GlobalScope.launch(Dispatchers.IO) {
            for (email in studentsList) {
                studentsCollection.document(email).get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val token = document.getString("token")
                        var name = document.getString("name")
                        if (token != null) {
                            handler.postDelayed({
                                val notificationsSender = SendNotification(token, "$userNameSurname - $courseID", notificationText, this@CommentsActivity)
                                notificationsSender.SendNotifications()
                            }, 300)

                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("FCM", "Error getting document: $e")
                }
            }
        }
    }

    private fun fetchEmailList() {
        val tasks = mutableListOf<Task<*>>()
        for (i in 1..totalGroupNumber.toInt()) {
            val groupName = "Group$i"
            val groupRef = db.collection("Courses").document(courseID).collection(groupName)
            val task = groupRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    snapshot.documents.forEach { document ->
                        val documentName = document.id
                        if (documentName.endsWith("@std.yildiz.edu.tr")) {
                            if (groupName == "Group") {

                                studentsList.add(documentName)
                            } else {

                                studentsList.add(documentName)
                            }
                        }
                    }
                } else {
                    println("No documents found in $groupName")
                }
            }.addOnFailureListener{
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

    private fun fetchCommentNumber() {
        val documentRef = db.collection("Courses").document(courseID).collection("Classroom").document(documentId)

        documentRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Firestore", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val newCommentNumber = snapshot.getString("commentnumber")
                if (newCommentNumber != null) {
                    commentNumber = newCommentNumber
                    Log.d("Firestore", "Comment number updated: $commentNumber")
                    val text = "$commentNumber Class Comments"
                    binding.classCommentText.text = text
                }
            } else {
                Log.d("Firestore", "Current data: null")
            }
        }

    }

    private fun saveComment() {
        val commentText = binding.commentInput.text.toString()
        notificationText = commentText
        if(commentText.isNotEmpty()){
            val comment = hashMapOf(
                "date" to Date(),
                "nameSurname" to userNameSurname,
                "comment" to commentText
            )
            val postRef = db.collection("Courses").document(courseID).collection("Classroom").document(documentId)

            val commentsRef = db.collection("Courses")
                .document(courseID)
                .collection("Classroom")
                .document(documentId)
                .collection("Comments")
            commentsRef.add(comment)
                .addOnSuccessListener {
                    Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show()
                    var temp = commentNumber.toInt()
                    temp += 1
                    commentNumber = temp.toString()
                    postRef.update("commentnumber", commentNumber)
                    binding.commentInput.text.clear()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        else{
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updatePostUI(){
        binding.userName.text = postNameSurname
        binding.userEmail.text = intent.getStringExtra("postEmail")
        binding.datePost.text = intent.getStringExtra("postDate")
        binding.textMainPost.text = intent.getStringExtra("postMainText")
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

    private fun getFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }
    private fun fetchComments() {
        val commentsRef =
            db.collection("Courses").document(courseID).collection("Classroom").document(documentId)
                .collection("Comments")
        commentsRef.orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                else{
                    if(value != null){
                        if(!value.isEmpty){
                            val documents = value.documents
                            commentList.clear()
                            for(document in documents){
                                val comment = document.getString("comment")
                                val nameSurname = document.getString("nameSurname")
                                val date = document.getDate("date")
                                val formattedDate = date?.let { getFormattedDate(it) }
                                if (comment != null && nameSurname != null && formattedDate != null) {
                                    val commentObject = CommentDataClass(formattedDate,nameSurname,comment)
                                    commentList.add(commentObject)
                                    println(commentList)
                                }

                            }
                            recyclerViewCommentsAdapter.notifyDataSetChanged()
                        }
                    }
                }

            }
    }

}