package com.huseyinemreseyrek.courseassistantapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityRegisterBinding


class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        firestore = Firebase.firestore
        auth = Firebase.auth
        binding.studentIdTextInputLayout.isEndIconVisible = false
        binding.emailTextInputLayout.isEndIconVisible = false

        binding.studentID.addTextChangedListener(object : TextWatcher {//yazdikca hatali mi dogru mu studentID giriliyor onu anlayan fonksiyon.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

               if(!binding.emailText.text.toString().endsWith("@yildiz.edu.tr")){
                   if (s.length != 8) { // ID degeri tam 8 digitten olusmali.
                       binding.studentIdTextInputLayout.error = "ID must be exactly 8 digits"
                       binding.studentIdTextInputLayout.isEndIconVisible = false
                   } else {
                       binding.studentIdTextInputLayout.error = null
                       binding.studentIdTextInputLayout.isEndIconVisible = true
                       binding.studentIdTextInputLayout.helperText = ""
                   }
               }
            }
        })

        binding.emailText.addTextChangedListener(object : TextWatcher { // yazdikca e-mailin formati dogru mu kontrol eden fonksiyon
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                if (checkEmail() == 0) {
                    binding.emailTextInputLayout.error = "Enter a valid e-mail"
                    binding.emailTextInputLayout.isEndIconVisible = false
                } else {
                    binding.emailTextInputLayout.error = null
                    binding.emailTextInputLayout.isEndIconVisible = true
                    binding.emailTextInputLayout.helperText = ""
                }
            }
        })

        binding.nameText.addTextChangedListener(object : TextWatcher { //Eger isim yerine rakam girilirse, uyari veriyor
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.any { it.isDigit() }) {
                    binding.nameTextInputLayout.error = "Do not use digit for Name!"
                } else {
                    binding.nameTextInputLayout.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.surnameText.addTextChangedListener(object : TextWatcher {//Eger soyisim yerine rakam girilirse uyari veriyor.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.any { it.isDigit() }) {
                    binding.surnameTextInputLayout.error = "Do not use digit for Surname!"
                } else {
                    binding.surnameTextInputLayout.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.passwordText.addTextChangedListener(object : TextWatcher { //sifre eger 8 digitten ufaksa register'a basilinca hata veriyor, kullanici tekrar yazmaya baslayinca error ifadesini kaldiriyor.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.passwordTextInputLayout.error = null
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })



    }


    private fun checkEmail(): Int{ //email dogru mu degil mi kontrolu yapiyor, ayrica hangi accountType'da hesap aciliyor onu kontrol ediyor
        var error: String? = null
        val email = binding.emailText.text.toString()
        return if(email.endsWith("@std.yildiz.edu.tr")){
            if(!binding.studentID.isEnabled){
                binding.studentID.isEnabled = true
            }
            1
        } else if(email.endsWith("@yildiz.edu.tr")){
            binding.studentIdTextInputLayout.error = null
            binding.studentID.setText("")
            binding.studentID.isEnabled = false

            2
        } else{
            if(!binding.studentID.isEnabled){
                binding.studentID.isEnabled = true
            }
            error = "Enter valid e-mail"
            0
        }
    }

    private fun checkPassword(): Boolean{ //Sifreyi kontrol ediyor.
        val password = binding.passwordText.text.toString()
        if(password.isEmpty()){
            println("Girdim mi?")
            binding.passwordTextInputLayout.error = "Enter a password"
            return false
        }
        else if(password.length < 8){
            binding.passwordTextInputLayout.error = "Password must be at least 8 characters"
            return false
        }
        else{
            binding.passwordTextInputLayout.error = null
        }
        return true

    }

    private fun controlRegister() : Boolean{ //Kayit butonuna basildiginda herhangi bir error yoksa kaydedecek varsa kaydetmeyecek
        val err1 = binding.nameTextInputLayout.error
        val err2 = binding.surnameTextInputLayout.error
        val err3 = binding.studentIdTextInputLayout.error
        val err4 = binding.emailTextInputLayout.error
        val err5 = binding.passwordTextInputLayout.error

        return err1 == null && err2 == null && err3== null && err4 == null && err5 == null
    }

    fun registerApply(view : View) : Boolean{ //kayit islemini gerceklestiren fonksiyon
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        if(binding.nameText.text.toString().isEmpty()){ //eger isim girilmediyse
            binding.nameTextInputLayout.error = "Name is required"
        }
        if(binding.surnameText.text.toString().isEmpty()){ // eger soyisim girilmediyse
            binding.surnameTextInputLayout.error = "Surname is required"
        }
        if(binding.emailText.text.toString().endsWith("@std.yildiz.edu.tr")){ //eger email student emailiyse ve studentID girilmediyse
            if(binding.studentID.text.toString().isEmpty()){
                binding.studentIdTextInputLayout.error = "StudentID is required"
            }
        }
        if(binding.emailText.text.toString().isEmpty()){ //eger email girilmediyse
            binding.emailTextInputLayout.error = "Email is required"
        }
        checkPassword() //sifre kontrol
        return if(controlRegister()){ //register yapabilir miyiz yapamaz miyiz kontrolu
            //firebase'de email sifre hesap acma islemleri.
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    sendEmailVerification()
                    if(binding.emailText.text.toString().endsWith("@std.yildiz.edu.tr")){
                        saveStudent()
                    }
                    else{
                        saveInstructor()
                    }
                    println("Basarili kayit")
                    finish() //register intentini kapatiyoruz.
                }else{
                    if(task.exception is FirebaseAuthUserCollisionException){
                        Toast.makeText(this,"This e-mail adress is already registered",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Registration failed, try again",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        } else{
            println("Kayit basarisiz")
            Toast.makeText(this,"Enter all informations correctly",Toast.LENGTH_SHORT).show()
            false
        }


    }
    private fun saveStudent(){
        val userMap = hashMapOf<String,Any>()
        userMap.put("name",binding.nameText.text.toString())
        userMap.put("surname",binding.surnameText.text.toString())
        userMap.put("email",binding.emailText.text.toString())
        userMap.put("accountType","Student")
        userMap.put("studentID",binding.studentID.text.toString())
        userMap.put("downloadUrl","")
        userMap.put("educationalInfo","")
        userMap.put("phoneNumber","")
        userMap.put("instagramAdress","")
        userMap.put("twitterAdress","")

        userMap.put("RegisteredCourses" ,emptyList<Map<String, Any>>() )
        firestore.collection("Students")
            .document(binding.emailText.text.toString())
            .set(userMap)
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@Register, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }
    private fun saveInstructor(){
        val userMap = hashMapOf<String,Any>()
        userMap.put("name",binding.nameText.text.toString())
        userMap.put("surname",binding.surnameText.text.toString())
        userMap.put("email",binding.emailText.text.toString())
        userMap.put("accountType","Instructor")
        userMap.put("downloadUrl","")
        userMap.put("educationalInfo","")
        userMap.put("phoneNumber","")
        userMap.put("instagramAdress","")
        userMap.put("twitterAdress","")
        /*val courses = mapOf(
            "BLM520" to mapOf(
                "courseName" to "Mobile Programming",
                "mainInstructor" to "Amac hoca",
                "courseID" to "BLM520",
                "group" to "1",
                "status" to "Attending"
            ),
            "BLM521" to mapOf(
                "courseName" to "Selamlar",
                "mainInstructor" to "MFA",
                "courseID" to "BLM521",
                "group" to "2",
                "status" to "Attending"
            )
        )
        userMap["RegisteredCourses"] = courses //kurslarin tipi*/
        userMap.put("RegisteredCourses" ,emptyList<Map<String, Any>>() )
        firestore.collection("Instructors")
            .document(binding.emailText.text.toString())
            .set(userMap)
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@Register, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        val dataRef = firestore.collection("Attendance").document(binding.emailText.text.toString())
        val initialData = hashMapOf<String, Any>() // Boş bir veri haritası

        dataRef.set(initialData)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot successfully written!")

                // İsteğe bağlı: Boş belge oluşturduktan sonra Attendances koleksiyonunu ekleyin
                val attendanceData = hashMapOf<String, Any>() // Boş bir veri haritası

                dataRef.collection("Attendances")
                    .add(attendanceData)
                    .addOnSuccessListener { documentReference ->
                        Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error adding document", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing document", e)
            }

        /*val newCourse = mapOf(
            "BLM522" to mapOf(
                "courseName" to "Mobile Programming2",
                "mainInstructor" to "Oya",
                "courseID" to "BLM522",
                "group" to "2",
                "status" to "Attending"
            )
        )
        firestore.collection("Instructors").document(binding.emailText.text.toString())
            .set(mapOf("RegisteredCourses" to newCourse), SetOptions.merge())
            .addOnSuccessListener {
                println("New course added successfully!")
            }
            .addOnFailureListener { e ->
                println("Error adding new course: $e")
            } //yeni kurs ekleme*/



    }

    private fun sendEmailVerification(){ //firebase ile emaildogrulamasi yollama
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(this,"Verification email sent",Toast.LENGTH_SHORT).show()
            }
        }
    }

}



