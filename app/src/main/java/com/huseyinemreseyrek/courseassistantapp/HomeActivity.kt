package com.huseyinemreseyrek.courseassistantapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityHomeBinding
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail : String
    private lateinit var user : Person

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        println("Home Activity calisti.")
        userEmail = intent.getStringExtra("userEmail")!!
        println("Home Activity icin $userEmail") //debug kontrol
        getData()

        setSupportActionBar(binding.homeActivityToolbar)
        binding.homeActivityToolbar.title = "Abcd"

        auth = Firebase.auth
        
    }

    override fun onResume() { //kullanici bilgilerini degistirince ekranda gosterelim guncellenmis seyleri
        super.onResume()
        getData()
        println("HomeActivity on resume calisiyor")


    }

    private fun getData() {
        println("Getting data for $userEmail")
        val docRef = if (userEmail.endsWith("@std.yildiz.edu.tr")) {
            db.collection("Students").document(userEmail)
        } else {
            db.collection("Instructors").document(userEmail)
        }

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
                val currentCourses = documentSnapshot.get("RegisteredCourses") as? MutableList<Map<String, Any>> ?: mutableListOf()
                user = if(accountType == "Instructor"){
                    Instructor(accountType,name,surname,email,downloadUrl,educationalInfo,phoneNumber,instagramAddress,twitterAddress,currentCourses)
                } else{
                    val studentID = documentSnapshot.getString("studentID") ?: "N/A"
                    Student(accountType,studentID,name,surname,email,downloadUrl,educationalInfo,phoneNumber,instagramAddress,twitterAddress,currentCourses)
                }

                showInfo()
            } else {
                println("Document does not exist.")
            }
        }.addOnFailureListener { e ->
            println("Error fetching document: ${e.localizedMessage}")
        }
    }

    private fun showInfo(){ //gerekli bilgileri gosterecek fonksiyon
        if(user.downloadUrl.isNotEmpty()){ //profil fotografi var ise onu gosterelim.
            Picasso.get().load(user.downloadUrl).into(binding.profilfotosu)
        }
        binding.educationalInfoTextView.text = user.educationalInfo
        binding.emailTextView.text = user.email
        binding.phoneTextView.text = user.phoneNumber
        val userNameSurname = user.name + " " + user.surName
        binding.nameTextView.text = userNameSurname
        if(user.twitterAdress.isNotEmpty()){//twitter adres var ise koyalim yok ise koymayalim.
            binding.twitterTextView.visibility = View.VISIBLE
            binding.twitterTextView.text = user.twitterAdress
        }
        else{
            binding.twitterTextView.visibility = View.INVISIBLE
        }
        if(user.instagramAdress.isNotEmpty()){//instagram adresi var ise koyalim yok ise koymayalim
            binding.instagramTextView.visibility = View.VISIBLE
            binding.instagramTextView.text = user.instagramAdress
        }
        else{
            binding.instagramTextView.visibility = View.INVISIBLE
        }
    }

    fun clickPhoneNumber(view: View){//numaraya tiklaninca whatsapp acan fonksiyon
        val appPackageName = "com.whatsapp"
        val intent = packageManager.getLaunchIntentForPackage(appPackageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            val uri = Uri.parse("market://details?id=$appPackageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(goToMarket)
        }
    }


    fun openOutlook(view : View) {//emaile tiklaninca outlook acan fonksiyon.
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.microsoft.office.outlook")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val appPackageName = "com.microsoft.office.outlook"
            val uri = Uri.parse("market://details?id=$appPackageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(goToMarket)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //menuyu bagliyoruz
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.app_menu,menu)
        println("oncreateoptions cagrildi")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //menude secilecek seylere gore yapilacak gerekli islemleri gerceklestiriyoruz.
        if(item.itemId == R.id.edit_profile){
            val intent = Intent(this,AccountInformation::class.java)
            intent.putExtra("userEmail",user.email)
            intent.putExtra("firstLogin",false)
            startActivity(intent)
        }else if(item.itemId == R.id.signout){
            auth.signOut()
            val intent = Intent(this,Login::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    fun goCourses(view: View) {

        val intent = Intent(this,InstructorCourses::class.java).apply{
            putExtra("userEmail",user.email)
        }
        startActivity(intent)

    }
}

