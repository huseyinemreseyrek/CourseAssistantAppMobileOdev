package com.huseyinemreseyrek.courseassistantapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityCourseInformationsBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityInstructorCoursesBinding
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.properties.Delegates

//grup numarasi

class CourseInformations : AppCompatActivity() {
    private lateinit var binding: ActivityCourseInformationsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var userEmail : String
    private lateinit var courseName : String
    private lateinit var courseID : String
    private lateinit var mainInstructor : String
    private lateinit var status : String
    private lateinit var groupNumber : String
    private lateinit var date : String
    private lateinit var selectedGroup: String
    private lateinit var totalGroupNumbers : String
    private var totalAttendingStudents = 0
    private lateinit var users : ArrayList<String>
    private lateinit var groupNames : ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseInformationsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail").toString()
        println(userEmail)
        println("Course Informations1")
        courseID = intent.getStringExtra("courseID").toString()
        println("Course Informations2")
        users = ArrayList()
        groupNames = ArrayList()
        getData()
        if(userEmail.endsWith("@std.yildiz.edu.tr")){
            binding.assignStudentsButton.visibility = View.GONE
            binding.updateButton.visibility = View.GONE
            binding.deleteStudent.visibility = View.GONE
            binding.deleteCourseButton.visibility = View.GONE
        }

        println("Course Informations3")

        println("Course Informations4")

    }
    private fun timestampToDateString(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getData() {
        println("Course Informations5")
        val userEmailDocRef = db.collection("Instructors").document(userEmail)
        val coursesDocRef = db.collection("Courses").document(courseID)
        val userEmailTask = userEmailDocRef.get()
        val coursesTask = coursesDocRef.get()
        Tasks.whenAllComplete(userEmailTask, coursesTask).addOnCompleteListener {
            if (it.isSuccessful) {
                val userDocument = userEmailTask.result
                val courseDocument = coursesTask.result

                if (userDocument.exists()) {
                    println("User data fetched")
                    val registeredCourses = userDocument.get("RegisteredCourses") as? Map<String, Map<String, Any>>
                    val registeredCourse = registeredCourses?.get(courseID)
                    courseName = registeredCourse?.get("courseID")?.toString() ?: "N/A"
                    mainInstructor = registeredCourse?.get("mainInstructor")?.toString() ?: "N/A"
                    groupNumber = registeredCourse?.get("group")?.toString() ?: "N/A"
                    status = registeredCourse?.get("status")?.toString() ?: "N/A"
                    date = registeredCourse?.get("date")?.let { timestampToDateString(it as Timestamp) } ?: "N/A"
                } else {
                    println("User document does not exist.")
                }

                if (courseDocument.exists()) {
                    println("Course data fetched")
                    totalGroupNumbers = courseDocument.getString("GroupNumbers")?.toString() ?: "0"
                } else {
                    println("Course document does not exist.")
                }

                updateUI()
            } else {
                println("Error fetching data: ${it.exception?.message}")
            }
        }
    }

    private fun updateUI() {
        binding.courseName.text = courseName
        val text1 = "Main Instructor : $mainInstructor"
        binding.mainInstructor.text = text1
        binding.groupNumber.text = if (groupNumber.isEmpty()) "Your Group : Not Assigned" else "Your Group : $groupNumber"
        binding.status.text = status
        val text2 = "Created At : $date"
        binding.dateText.text = text2
        val text3 = "Course ID : $courseID"
        binding.courseID.text = text3
    }
    /*private fun getNumberOfAttendingStudents(){
        if(groupNumber.isNotEmpty()){

            val courseDocRef = db.collection("Courses").document(courseID).collection("Group$groupNumber")

            courseDocRef.get()
                .addOnSuccessListener { documents ->
                    val filteredDocs = documents.filter {
                        (it.get("email") as? String)?.endsWith("@std.yildiz.edu.tr") == true
                    }
                    totalAttendingStudents = filteredDocs.size
                    val text = "Number of Students in the Group$groupNumber : $totalAttendingStudents"
                    binding.numberOfStudents.text = text
                    Log.d("EmailCount", "There are $totalAttendingStudents students in the group")
                }
                .addOnFailureListener { exception ->
                    Log.w("Error", "Error getting documents: ", exception)
                }
        }
        else {
            val studentCount = mutableListOf<Int>()
            var totalStudent  = 0
            val groups = mutableListOf("Group")
            for (i in 1..totalGroupNumbers.toInt()) {
                groups.add("Group$i")
            }
            for (group in groups) {
                val courseDocRef = db.collection("Courses").document(courseID).collection(group)
                courseDocRef.get()
                    .addOnSuccessListener { documents ->
                        val count = documents.count {
                            (it.get("email") as? String)?.endsWith("@std.yildiz.edu.tr") == true
                        }
                        studentCount.add(count)
                        if (studentCount.size == groups.size) {
                            totalStudent = studentCount.sum()
                            val text = "Total Number of Students: $totalStudent"
                            binding.numberOfStudents.text = text
                        }

                    }.addOnFailureListener { exception ->
                        Log.w("Error", "Error getting documents: ", exception)
                    }
            }
        }

    }*/


    fun updateCourseInformations(view : View){
        if(userEmail == mainInstructor){
            val intent = Intent(this, UpdateCourseInformations::class.java)
            intent.putExtra("userEmail", userEmail)
            intent.putExtra("courseID", courseID)
            startActivity(intent)
        }
        else{
            Toast.makeText(this, "You are not the founder instructor", Toast.LENGTH_SHORT).show()
        }

    }

    fun showInstructors(view : View){
        val intent = Intent(this, ShowInstructorsActivity::class.java)
        intent.putExtra("userEmail", userEmail)
        intent.putExtra("courseID", courseID)
        intent.putExtra("totalGroupNumbers", totalGroupNumbers)
        intent.putExtra("mainInstructor",mainInstructor)
        startActivity(intent)
    }

    private fun showGroupDialog() {
        if(!userEmail.endsWith("@std.yildiz.edu.tr")){
            val groups = mutableListOf("All Group")

            for(i in 1..totalGroupNumbers.toInt()){
                groups.add("Group$i")
            }
            val groupsArray = groups.toTypedArray()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choose a group")
            builder.setItems(groupsArray) { dialog, which ->
                selectedGroup = groups[which]
                Toast.makeText(this, "You selected: $selectedGroup", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ShowStudentsActivityInstructor::class.java)
                intent.putExtra("userEmail", userEmail)
                intent.putExtra("courseID", courseID)
                intent.putExtra("selectedGroup", selectedGroup)
                intent.putExtra("totalGroupNumbers",totalGroupNumbers)
                startActivity(intent)
            }
            builder.show()
        }
        else{
            val intent = Intent(this, ShowStudentsActivityInstructor::class.java)
            intent.putExtra("userEmail", userEmail)
            intent.putExtra("courseID", courseID)
            intent.putExtra("selectedGroup", selectedGroup)
            startActivity(intent)
        }


    }

    fun showStudents(view : View){
        showGroupDialog()
    }

    fun assignStudents(view : View){
        if(groupNumber.isEmpty()){
            Toast.makeText(this, "You have no authority to assign a Student", Toast.LENGTH_SHORT).show()
        }
        else if(groupNumber == "Founder"){
            Toast.makeText(this, "You are the founder instructor", Toast.LENGTH_SHORT).show()
        }
        else{
            val intent = Intent(this, AssignStudentsActivity::class.java)
            intent.putExtra("userEmail", userEmail)
            intent.putExtra("courseID", courseID)
            intent.putExtra("groupNumber", groupNumber)
            intent.putExtra("mainInstructor",mainInstructor)
            intent.putExtra("courseName",courseName)
            intent.putExtra("status",status)
            startActivity(intent)
        }
    }

    fun deleteStudent(view : View){
        if(groupNumber.isEmpty()){
            Toast.makeText(this, "You have no authority to delete a Student", Toast.LENGTH_SHORT).show()
        }
        else{
            val intent = Intent(this, DeleteStudentActivity::class.java)
            intent.putExtra("userEmail", userEmail)
            intent.putExtra("courseID", courseID)
            intent.putExtra("groupNumber", groupNumber)
            startActivity(intent)
        }
    }

    private fun x(): Task<Void> {
        val tasks = mutableListOf<Task<*>>()
        val subCollections = mutableListOf("Group")
        for (i in 1..totalGroupNumbers.toInt()) {
            subCollections.add("Group$i")
        }
        val courseDocRef = db.collection("Courses").document(courseID)
        for (collectionName in subCollections) {
            val collectionRef = courseDocRef.collection(collectionName)
            val task = collectionRef.get().continueWithTask { task ->
                if (task.isSuccessful) {
                    val batch = db.batch()
                    for (document in task.result) {
                        batch.delete(document.reference)
                    }
                    batch.commit()
                } else {
                    throw task.exception ?: Exception("Failed to fetch documents in collection: $collectionName")
                }
            }
            tasks.add(task)
        }

        // Tüm alt koleksiyonlar ve belgeler silindikten sonra ana belgeyi sil
        Tasks.whenAll(tasks).continueWithTask {
            if (it.isSuccessful) {
                courseDocRef.delete()
            } else {
                throw it.exception ?: Exception("Failed to delete subcollections")
            }
        }.addOnSuccessListener {
            Log.d("DeleteCourse", "Course and all subcollections successfully deleted")
        }.addOnFailureListener { e ->
            Log.w("DeleteCourse", "Error deleting course and subcollections", e)
        }
        return Tasks.whenAll(tasks)
    }
    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    private fun deleteCourseDocument() {
        fetchAllEmails().continueWithTask { task ->
            if (task.isSuccessful) {

                return@continueWithTask x()
            } else {
                throw task.exception ?: Exception("Failed to fetch emails")
            }
        }.continueWithTask { task ->
            if (task.isSuccessful) {
                return@continueWithTask updateUserDocuments()
            } else {
                throw task.exception ?: Exception("Failed to delete course document")
            }
        }.addOnSuccessListener {
            Log.d("DeleteCourse", "All operations completed successfully")
        }.addOnFailureListener { e ->
            Log.w("DeleteCourse", "Error during the operations: ${e.message}")
        }
    }



    private fun updateUserDocuments(): Task<Void> {
        val tasks = mutableListOf<Task<*>>()
        var remainingUpdates = users.size

        if (remainingUpdates == 0) {
            return Tasks.forResult(null) // Eğer güncellenecek kullanıcı yoksa, işlemi hemen tamamla
        }

        for (email in users) {
            val userDocRef: DocumentReference = if (email.endsWith("@std.yildiz.edu.tr")) {
                db.collection("Students").document(email)
            } else {
                db.collection("Instructors").document(email)
            }
            val updateMap = mapOf("RegisteredCourses.$courseID" to FieldValue.delete())
            val task = userDocRef.update(updateMap)
                .addOnSuccessListener {
                    Log.d("FirestoreUpdate", "Specific course map removed successfully.")
                    remainingUpdates--
                    if (remainingUpdates == 0) {
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("FirestoreUpdate", "Error removing course map", e)
                    remainingUpdates--
                    if (remainingUpdates == 0) {
                        finish()
                    }
                }
            tasks.add(task)
        }

        return Tasks.whenAll(tasks) // Tüm güncelleme görevlerinin tamamlanmasını bekle
    }

    private fun fetchAllEmails(): Task<Void> {
        val tasks = mutableListOf<Task<*>>()
        users.add(mainInstructor)
        for (i in 0..totalGroupNumbers.toInt()) {
            val groupName = if (i == 0) "Group" else "Group$i"
            val groupRef = db.collection("Courses").document(courseID).collection(groupName)

            val task = groupRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    snapshot.documents.forEach { document ->
                        val documentName = document.id
                        if (!documentName.endsWith("@std.yildiz.edu.tr")) {
                            users.add(documentName)
                        }
                    }
                } else {
                    println("No documents found in $groupName")
                }
            }.addOnFailureListener {
                println("Failed to fetch documents for $groupName: ${it.message}")
            }
            tasks.add(task)
        }
        return Tasks.whenAll(tasks)
    }

    fun deleteCourse(view : View){
        if(userEmail == mainInstructor){
            showProgress(true)
            showConfirmationDialog(context = this, process = {
                deleteCourseDocument()
            })
            showProgress(false)
        }
        else{
            Toast.makeText(this, "Only founder instructor can delete a course", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showConfirmationDialog(context: Context, process: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Last Choice")
        builder.setMessage("Are you sure about that?")

        builder.setPositiveButton("Yes") { dialog, which ->
            process()
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }



}