package com.huseyinemreseyrek.courseassistantapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityHomeBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityInstructorCoursesBinding
import java.sql.Time

class InstructorCourses : AppCompatActivity() {
    private lateinit var recyclerViewCoursesAdapter : RecyclerViewCoursesAdapter
    private lateinit var coursesNamesList : ArrayList<CourseNames>
    private lateinit var binding: ActivityInstructorCoursesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail : String
    private lateinit var db: FirebaseFirestore
    private var flag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstructorCoursesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore

        userEmail = intent.getStringExtra("userEmail")!!
        println("InstructorCourses Activity icin $userEmail") //
        setSupportActionBar(binding.myToolbar)
        auth = Firebase.auth

        binding.myToolbar.setNavigationOnClickListener {
            showNavigationMenu(it)
        }

        coursesNamesList = ArrayList()

        println(coursesNamesList)

        binding.coursesList.layoutManager = LinearLayoutManager(this)

        recyclerViewCoursesAdapter = RecyclerViewCoursesAdapter(coursesNamesList,userEmail)
        binding.coursesList.adapter = recyclerViewCoursesAdapter

        prepareCourses()


    }


    private fun prepareCourses() {
        var collection = ""
        if(userEmail.endsWith("@std.yildiz.edu.tr")){
            collection = "Students"
        }
        else{
            collection = "Instructors"
        }
        println("kontrol1")
        db.collection(collection).document(userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    coursesNamesList.clear()
                    val instructorCourses = snapshot.data?.get("RegisteredCourses") as? Map<String, Map<String, Any>>
                    if (instructorCourses != null) {
                        for ((courseName, courseDetails) in instructorCourses) {
                            val name = courseDetails["courseName"] as String
                            val courseID = courseDetails["courseID"] as String
                            val mainInstructor = courseDetails["mainInstructor"] as String
                            val status = courseDetails["status"] as String
                            val  date  = courseDetails["date"] as Timestamp
                            val course = CourseNames(name,
                                courseID, mainInstructor, status, date
                            )
                            coursesNamesList.add(course)
                            println(coursesNamesList)
                        }
                    } else {
                        coursesNamesList.add(CourseNames("No Registered Courses", "You should register a Course", "Contact with University", "If there is an Error",
                            Timestamp.now()))
                    }
                    recyclerViewCoursesAdapter.notifyDataSetChanged()
                } else {
                    Log.d(TAG, "Current data: null")
                    coursesNamesList.add(CourseNames("No Registered Courses", "You should register a Course", "Contact with University", "If there is an Error",
                        Timestamp.now()))
                }
            }
    }

    private fun prepareCoursesFilterByYear() {
        TODO("Not yet implemented")
    }

    private fun prepareCoursesDescendingOrderByDate() {
        coursesNamesList.sortByDescending { it.date.toDate().time }
        recyclerViewCoursesAdapter.notifyDataSetChanged()
    }

        private fun showNavigationMenu(view: View) {

        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.filter_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.descending_order -> {

                    if(flag == 0 || flag == 2){
                        Toast.makeText(this, "Descending order selected", Toast.LENGTH_SHORT).show()
                        prepareCoursesDescendingOrderByDate()
                        flag = 1
                    }
                    else if(flag == 1){
                        Toast.makeText(this, "Descending order unselected", Toast.LENGTH_SHORT).show()
                        prepareCourses()
                        flag = 0
                    }

                    true
                }
                R.id.filter_by_year -> {
                    Toast.makeText(this, "Filter by year selected", Toast.LENGTH_SHORT).show()
                    if(flag == 0 || flag == 1){
                        prepareCoursesFilterByYear()
                        flag = 2
                    }
                    else if(flag == 2){
                        prepareCourses()
                        flag = 0
                    }
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.instructor_course_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_course){
            if(!userEmail.endsWith("@std.yildiz.edu.tr")){
                    val intent = Intent(this@InstructorCourses, AddCourse::class.java)
                    intent.putExtra("email",userEmail)
                    startActivity(intent)
            }
            else{
                Toast.makeText(this, "You are not an instructor", Toast.LENGTH_SHORT).show()
            }
        }
        if(item.itemId == R.id.sign_out){
            auth.signOut()
            val intent = Intent(this,Login::class.java)
            startActivity(intent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

}