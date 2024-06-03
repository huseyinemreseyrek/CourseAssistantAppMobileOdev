package com.huseyinemreseyrek.courseassistantapp

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAccountInformationBinding
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.typeOf

class AccountInformation : AppCompatActivity() {

    private lateinit var binding: ActivityAccountInformationBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var pendingAction: Runnable? = null
    var firstLogin : Boolean = false
    private lateinit var userEmail : String
    private lateinit var db: FirebaseFirestore
    private lateinit var storage : FirebaseStorage
    private lateinit var user : Person
    private lateinit var currentPhotoPath: String
    var selectedPicture : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("calisiyor mu accountInfo")
        binding = ActivityAccountInformationBinding.inflate(layoutInflater)
        println("calisiyor mu accountInfo2")
        val view = binding.root
        setContentView(view)
        println("calisiyor mu accountInfo3")
        db = Firebase.firestore
        storage = Firebase.storage
        println("calisiyor mu accountInfo4")
        userEmail = intent.getStringExtra("userEmail")!!

        /*setSupportActionBar(binding.accountInfoToolbar)
        binding.accountInfoToolbar.title = "Account Information"*/
        getData()
        println("calisiyor mu accountInfo5")

        firstLogin = intent.getBooleanExtra("firstLogin",false)


        val degrees = listOf("Undergraduate", "Master's Degree", "PhD & semester")
        val adapter = ArrayAdapter(this, R.layout.list_item, degrees)
        val autoCompleteTextView = binding.educationalInformationAutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)


        println("Buraya kadar sorunsuz mu")

        binding.phoneNumberEditText.addTextChangedListener(object : TextWatcher {//phoneNumber girilirken kontrol et
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                if (s.length != 11) {
                    binding.phoneNumberTextInputLayout.error = "Phone Number must be exactly 11 digits"
                } else {
                    binding.phoneNumberTextInputLayout.error = null

                }

            }
        })

        registerLaunchers()

    }
    private fun setupUserDetails() {
        binding.accountTypeEditText.setText(user.accountType)

        if(user.accountType == "Student"){ //kullanici student ise studentID gosterelim
            binding.studentIDEditText.setText((user as Student).studentID)
        }
        else{ //kullanici Instructor ise studentID yok olsun
            binding.studentIDTextInputLayout.visibility = View.GONE
        }
        binding.nameEditText.setText(user.name)
        binding.surnameEditText.setText((user.surName))
        binding.emailEditText.setText(user.email)
        if(user.educationalInfo.isNotEmpty()){
            binding.educationalInformationAutoCompleteTextView.setText(user.educationalInfo)
        }
        if(user.phoneNumber.isNotEmpty()){
            binding.phoneNumberEditText.setText(user.phoneNumber)
        }
        if(user.instagramAdress.isNotEmpty()){
            binding.instagramEditText.setText(user.instagramAdress)
        }
        if(user.twitterAdress.isNotEmpty()){
            binding.twitterEditText.setText(user.twitterAdress)
        }
        if(user.downloadUrl.isNotEmpty()){ //profil fotosu var ise goster.
            Picasso.get().load(user.downloadUrl).into(binding.profilePhoto)
        }
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
                val instagramAddress = documentSnapshot.getString("instagramAdress") ?: ""
                val twitterAddress = documentSnapshot.getString("twitterAdress") ?: ""
                val currentCourses = documentSnapshot.get("RegisteredCourses") as? MutableList<Map<String, Any>> ?: mutableListOf()
                user = if(accountType == "Instructor"){
                    Instructor(accountType,name,surname,email,downloadUrl,educationalInfo,phoneNumber,instagramAddress,twitterAddress,currentCourses)
                } else{
                    val studentID = documentSnapshot.getString("studentID") ?: "N/A"
                    Student(accountType,studentID,name,surname,email,downloadUrl,educationalInfo,phoneNumber,instagramAddress,twitterAddress,currentCourses)
                }

                setupUserDetails()  // User nesnesi oluşturulduktan sonra ilgili UI güncellemelerini yapar.
            } else {
                println("Document does not exist.")
            }
        }.addOnFailureListener { e ->
            println("Error fetching document: ${e.localizedMessage}")
        }
    }


    //Buradan sonrasi tamamen izinlerle ilgili normal fonksiyonlar

    private fun openGallery(){
        val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intentGallery)
    }
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: throw IOException("Failed to get external storage directory")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {

            currentPhotoPath = absolutePath
        }
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e(TAG, "Photo file creation failed", ex)
            null
        }
        photoFile?.also {
            val photoURI = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                it
            )
            val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            activityResultLauncher.launch(intentCamera)
        }
    }

    private fun registerLaunchers(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                val intentResult = result.data
                if(intentResult != null){
                    selectedPicture = intentResult.data
                    selectedPicture?.let {
                        binding.profilePhoto.setImageURI(it)
                    }
                }else{
                    selectedPicture = Uri.fromFile(File(currentPhotoPath))
                    binding.profilePhoto.setImageURI(selectedPicture)
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted){
                checkAndPerformPendingAction()
            } else {
                Toast.makeText(this@AccountInformation, "Permission needed for this action!", Toast.LENGTH_LONG).show()
                pendingAction = null
            }
        }
    }

    private fun checkAndPerformPendingAction() {
        when (pendingAction) {
            is Runnable -> pendingAction?.run()
        }
        pendingAction = null
    }

    private fun openGallery2(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 ve üzeri için
            pendingAction = Runnable { openGallery() }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                openGallery()
            }
        } else { // Android 13 altı için
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                pendingAction = Runnable { openGallery() }
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                openGallery()
            }
        }
    }

    private fun openCamera2(view: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pendingAction = Runnable { openCamera() }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Snackbar.make(view, "Camera permission is needed to take pictures", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }.show()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            openCamera()
        }
    }


    fun showImageSourceDialog(view: View) { //Galeriden mi Kameradan mi fotograf koymak istiyor?
        val items = arrayOf("Select from Gallery", "Take Photo with Camera")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose your image source")
        builder.setItems(items) { dialog, which ->
            when (which) {
                0 -> { // Galeri
                    openGallery2(view)
                }
                1 -> { // Kamera
                    openCamera2(view)
                }
            }
        }
        builder.show()
    }


    private fun checkInfo() : Boolean{ //infolar dogru girilmis mi kontrol et
        if(binding.nameEditText.text.toString().isEmpty()){
            return false
        }
        if(binding.surnameEditText.text.toString().isEmpty()){
            return false
        }
        if(binding.phoneNumberEditText.text.toString().isEmpty()){
            return false
        }
        if(binding.phoneNumberEditText.text.toString().length != 11){
            binding.phoneNumberTextInputLayout.error = "Enter the phone number correctly"
            return false
        }
        if(binding.educationalInformationAutoCompleteTextView.text.toString().isEmpty()){
            return false
        }

        return true



    }
    private fun getDocumentReference(): DocumentReference {
        return if (userEmail.endsWith("@std.yildiz.edu.tr")) {
            db.collection("Students").document(userEmail)
        } else {
            db.collection("Instructors").document(userEmail)
        }
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun saveInfo(view: View) {
        if (checkInfo()) { // Önce bilgi kontrolü yapılıyor
            showProgress(true)
            val userRef = getDocumentReference()
            val updates = mutableMapOf(
                "name" to binding.nameEditText.text.toString(),
                "surname" to binding.surnameEditText.text.toString(),
                "educationalInfo" to binding.educationalInformationAutoCompleteTextView.text.toString(),
                "phoneNumber" to binding.phoneNumberEditText.text.toString(),
                "instagramAdress" to binding.instagramEditText.text.toString(),
                "twitterAdress" to binding.twitterEditText.text.toString()
            )

            // Profil resmi yükleniyorsa, bu işlemi başlat
            if (selectedPicture != null) {
                val imageName = "$userEmail.jpg"
                val imageReference = storage.reference.child("profilePhotos").child(imageName)
                imageReference.putFile(selectedPicture!!).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageReference.downloadUrl
                }.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    updates["downloadUrl"] = downloadUrl
                    updateUserAttributes(userRef, updates)
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload profile photo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {

                updateUserAttributes(userRef, updates)
            }
        } else {
            Toast.makeText(this, "Fill all the required fields", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUserAttributes(userRef: DocumentReference, updates: Map<String, Any>) {
        userRef.update(updates).addOnSuccessListener {
            Toast.makeText(this, "User attributes updated successfully.", Toast.LENGTH_SHORT).show()
            showProgress(false)
            if(firstLogin){
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("userEmail",userEmail)
                startActivity(intent)
                finish()
            }
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to update user attributes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}