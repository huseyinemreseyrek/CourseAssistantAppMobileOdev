package com.huseyinemreseyrek.courseassistantapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAddCourseBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAddInstructorToCourseBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityInstructorCoursesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AddInstructorToCourse : AppCompatActivity() {
    private var flag = 0
    private lateinit var db: FirebaseFirestore
    private val editTextList = mutableListOf<Pair<EditText, EditText>>()
    private var instructorAndGroupNumber = mutableListOf<Pair<String, String>>()
    private lateinit var binding: ActivityAddInstructorToCourseBinding
    private lateinit var control: String
    private lateinit var userEmail: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInstructorToCourseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail") ?: ""
        binding.saveButton.isClickable = false
        binding.createFieldsButton.setOnClickListener {

            binding.fieldsContainer.removeAllViews()
            editTextList.clear()
            val fieldCount = binding.numberInput.text.toString().toIntOrNull() ?: 0
            if (fieldCount > 10) {
                Toast.makeText(this, "You can not assign more than 12 teacher", Toast.LENGTH_SHORT)
                    .show()
            } else {
                for (i in 1..fieldCount) {
                    val rowLayout = LinearLayout(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.HORIZONTAL
                    }

                    val editText = EditText(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1.0f
                        )
                        hint = "Instructor E-mail #$i"
                    }

                    val numberEditText = EditText(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1.0f
                        )
                        hint = "Group Number #$i"
                        inputType = InputType.TYPE_CLASS_NUMBER
                    }

                    rowLayout.addView(editText)
                    rowLayout.addView(numberEditText)
                    binding.fieldsContainer.addView(rowLayout)
                    editTextList.add(Pair(editText, numberEditText))
                }
            }
            binding.saveButton.isClickable = true
        }
    }

    // Bu metodu, saklanan EditText çiftlerine erişmek için kullanabilirsiniz
    private fun getEditTextStringValues() {
        instructorAndGroupNumber.clear()
        editTextList.forEachIndexed { index, pair ->
            val text = pair.first.text.toString()
            val number = pair.second.text.toString()
            instructorAndGroupNumber.add(Pair(text, number))
            println("Pair $index -> Text: $text, Number: $number")
        }
    }

    fun saveFun(view: View) {
        flag = 0
        editTextList.forEachIndexed { index, pair ->
            val instructor = pair.first.text.toString()
            if (instructor.isEmpty()) {
                flag = 1
            }
            if(instructor == userEmail){
                flag = 1
                Toast.makeText(this,"Don't enter the founder",Toast.LENGTH_SHORT).show()
            }
        }
        if(flag == 1){
            Toast.makeText(this,"Fill the all Instructors",Toast.LENGTH_SHORT).show()
        }
         if(flag!=1){
            CoroutineScope(Dispatchers.IO).launch {
                getEditTextStringValues()
                checkInstructorsExistence()
                withContext(Dispatchers.Main) {
                    if (flag == 0) {
                        val returnIntent = Intent()
                        returnIntent.putExtra("instructors", ArrayList(instructorAndGroupNumber))
                        setResult(Activity.RESULT_OK, returnIntent)
                        Toast.makeText(this@AddInstructorToCourse, "Instructors assigned", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddInstructorToCourse, "Document for $control does not exist!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

    }
    private suspend fun checkInstructorsExistence() {
        for (pair in instructorAndGroupNumber) {
            val searchString = pair.first
            val docRef = db.collection("Instructors").document(searchString)
            try {
                val document = docRef.get().await()
                if (!document.exists()) {
                    control = searchString
                    flag = 1
                    break
                } else {
                    Log.d("Firestore", "Document for $searchString exists!")
                }
            } catch (e: Exception) {
                Log.d("Firestore", "Failed with: ", e)
            }
        }
    }
}