package com.huseyinemreseyrek.courseassistantapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityLoginBinding
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityRegisterBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        SharedPreferencesClass.saveObject(applicationContext,Admin.admin.email,Admin.admin)
        println("Oncreate calisiyor.")
        auth = Firebase.auth

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

    override fun onResume() {
        super.onResume()
        println("Onresume calisiyor.")

    }
    private fun loginUser() {
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) { //email ve sifre girilmis mi kontrolu
            if(email!= "admin" && password != "admin"){
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) { //giris yapilabilecek mi kontrolu
                    val user = auth.currentUser

                    if (user != null && user.isEmailVerified) { //email dogrulanmis mi? eger dogrulanmadiysa giremez.
                        val userAtMemory = SharedPreferencesClass.getObject(applicationContext, email) //ilk defa mi giris yapilacak kontrol etmek icin(ilk defa giris yapilacaksa direkt olarak accountinformation aktivitesine yonlendirilecek

                        if (userAtMemory.getPhoneNumber().isEmpty()) {//ilk defa giris yapiyorsa phone Number kismi bostur.(doldurmasi zorunlu alandi)
                            Toast.makeText(this, "Please complete your account information.", Toast.LENGTH_SHORT).show()
                            val accountInfoIntent = Intent(this, AccountInformation::class.java)
                            accountInfoIntent.putExtra("userEmail", email)
                            accountInfoIntent.putExtra("firstLogin", true) //ilk defa giris yapiliyor.
                            startActivity(accountInfoIntent)//accountInfo yerine intent yap.
                            finish()

                        } else { //ilk defa girilmiyorsa, daha onceden girilmisse direkt homeActivity aktivitesine gidiyoruz.
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            val homePageIntent = Intent(this, HomeActivity::class.java)
                            homePageIntent.putExtra("userEmail", email)
                            homePageIntent.putExtra("firstLogin", false)
                            startActivity(homePageIntent)
                            finish()
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
                val homePageIntent = Intent(this, HomeActivity::class.java)
                homePageIntent.putExtra("userEmail", Admin.admin.email)
                homePageIntent.putExtra("firstLogin", false)
                startActivity(homePageIntent)
                finish()

            }
        } else {//eger hicbir sey girmediyse.
            Toast.makeText(this, "Enter the email and password", Toast.LENGTH_SHORT).show()
        }
    }
}