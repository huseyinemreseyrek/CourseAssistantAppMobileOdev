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
import com.google.firebase.ktx.Firebase

import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var user : Person
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private var userEmail : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        println("Home Activity calisti.")
        userEmail = intent.getStringExtra("userEmail")

        println("Home Activity icin $userEmail") //debug kontrol
        user = SharedPreferencesClass.getObject(applicationContext,userEmail!!) //user bilgilerini cekiyoruz.

        showInfo()

        setSupportActionBar(binding.myToolbar)
        binding.myToolbar.title = "CourseAssistantApp"

        auth = Firebase.auth
    }

    override fun onResume() { //kullanici bilgilerini degistirince ekranda gosterelim guncellenmis seyleri
        super.onResume()
        println("HomeActivity on resume calisiyor")
        user = SharedPreferencesClass.getObject(applicationContext,userEmail!!) //guncellenmis user'i cek
        showInfo()
    }

    private fun showInfo(){ //gerekli bilgileri gosterecek fonksiyon
        if(user.getProfilePicture() != null){ //profil fotografi var ise onu gosterelim.
            val bitmap = BitmapFactory.decodeByteArray(user.getProfilePicture(),0, user.getProfilePicture()!!.size)
            binding.profilfotosu.setImageBitmap(bitmap)
        }
        binding.educationalInfoTextView.text = user.getEducationalInfo()
        binding.emailTextView.text = user.email
        binding.phoneTextView.text = user.getPhoneNumber()
        val userNameSurname = user.name + " " + user.surName
        binding.nameTextView.text = userNameSurname
        if(user.getTwitterAdress().isNotEmpty()){//twitter adres var ise koyalim yok ise koymayalim.
            binding.twitterTextView.visibility = View.VISIBLE
            binding.twitterTextView.text = user.getTwitterAdress()
        }
        else{
            binding.twitterTextView.visibility = View.INVISIBLE
        }
        if(user.getInstagramAdress().isNotEmpty()){//instagram adresi var ise koyalim yok ise koymayalim
            binding.instagramTextView.visibility = View.VISIBLE
            binding.instagramTextView.text = user.getInstagramAdress()
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
}