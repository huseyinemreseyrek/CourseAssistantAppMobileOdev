package com.huseyinemreseyrek.courseassistantapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityLoginBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityRegisterBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val REQUEST_CODE = 1
    private val PREFS_NAME = "MyPrefs"
    private val KEY_NOTIFICATION_PERMISSION = "notification_permission"
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val preferences: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val isPermissionAskedBefore = preferences.getBoolean(KEY_NOTIFICATION_PERMISSION, false)

            if (!isPermissionAskedBefore) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
                } else {
                    // İzin zaten verilmiş
                    savePermissionAsked()
                }
            }
        }
        println("Oncreate calisiyor.")
        auth = Firebase.auth
        db = Firebase.firestore

        binding.loginButton.setOnClickListener { //login butonuna basildiginda ne olacagi
            loginUser()

        }
        binding.signUpTextView.setOnClickListener{ //sign Up'a basilirsa register aktivitesine gidiyoruz.
            val intent = Intent(this,Register::class.java)
            startActivity(intent)

        }

        binding.forgotPasswordView.setOnClickListener {//Sifre unutmaya basilirsa ResetPassword aktivitesine gidiyoruz.
            val intent = Intent(this, ResetPassword::class.java)
            startActivity(intent)
        }

    }

    private fun savePermissionAsked() {
        val preferences: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(KEY_NOTIFICATION_PERMISSION, true)
        editor.apply()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
            }
            savePermissionAsked()
        }
    }

    override fun onResume() {
        super.onResume()
        println("Onresume calisiyor.")

    }
    private fun getDocumentReference(): DocumentReference {
        val email = binding.emailText.text.toString()
        return if (email.endsWith("@std.yildiz.edu.tr")) {
            db.collection("Students").document(email)
        } else {
            db.collection("Instructors").document(email)
        }
    }


    private fun saveTokenToFirestore(email: String, token: String) {
        val db = Firebase.firestore
        val userDocRef = db.collection("Students").document(email)
        userDocRef.update("token", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token successfully saved")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error saving token", e)
            }
    }

    private fun loginUser() {
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) { //email ve sifre girilmis mi kontrolu
            if(email!= "admin" && password != "admin"){
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) { //giris yapilabilecek mi kontrolu
                    val user = auth.currentUser
                    val docRef = if (email.endsWith("@std.yildiz.edu.tr")) {
                        db.collection("Students").document(email)
                    } else {
                        db.collection("Instructors").document(email)
                    }
                    if(email.endsWith("@std.yildiz.edu.tr")){
                        user?.let {
                            // Kullanıcı giriş yaptıktan sonra token'ı al ve Firestore'a kaydet
                            FirebaseMessaging.getInstance().token
                                .addOnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        Log.w("FCM", "Fetching FCM token failed", task.exception)
                                        return@addOnCompleteListener
                                    }

                                    // Get new FCM registration token
                                    val token = task.result
                                    Log.d("FCM", "FCM Token: $token")
                                    saveTokenToFirestore(it.email!!, token)
                                }
                        }
                    }
                    if (user != null /*&& user.isEmailVerified*/) { //email dogrulanmis mi? eger dogrulanmadiysa giremez.
                        println("96. satir calisti")
                        docRef.get().addOnSuccessListener { document ->
                            if (document.exists() && document.getString("phoneNumber") == "") {
                                Toast.makeText(this, "Please complete your account information.", Toast.LENGTH_SHORT).show()
                                val accountInfoIntent = Intent(this, AccountInformation::class.java)
                                accountInfoIntent.putExtra("userEmail", email)
                                accountInfoIntent.putExtra("firstLogin", true)
                                startActivity(accountInfoIntent)
                                finish()
                            } else {
                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                                val homePageIntent = Intent(this, HomeActivity::class.java)
                                homePageIntent.putExtra("userEmail", email)
                                homePageIntent.putExtra("firstLogin", false)
                                startActivity(homePageIntent)
                                finish()
                            }

                        }



                    } else {//eger email dogrulanmadiysa, emailinizi dogrulayin uyarisi yapmaliyiz.
                        Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_SHORT).show()
                    }
                } else { //eger yanlis id sifre girdiyse
                    Toast.makeText(this, "Enter the correct e-mail or password.", Toast.LENGTH_SHORT).show()
                }
                }
            }else{
                Toast.makeText(this, "ADMIN LOGIN SUCCESFULL!", Toast.LENGTH_SHORT).show()
                ////val homePageIntent = Intent(this, HomeActivity::class.java)
                ////homePageIntent.putExtra("userEmail", Admin.admin.email)
                ////homePageIntent.putExtra("firstLogin", false)
                ////startActivity(homePageIntent)
                finish()

            }
        } else {//eger hicbir sey girmediyse.
            Toast.makeText(this, "Enter the email and password", Toast.LENGTH_SHORT).show()
        }
    }
}