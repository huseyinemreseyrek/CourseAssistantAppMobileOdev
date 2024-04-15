package com.huseyinemreseyrek.courseassistantapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAccountInformationBinding
import java.io.ByteArrayOutputStream

class AccountInformation : AppCompatActivity() {

    private lateinit var binding: ActivityAccountInformationBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var pendingAction: Runnable? = null
    private lateinit var user : Person
    var selectedBitmap : Bitmap? = null //profil fotografi icin
    var firstLogin : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountInformationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val userEmail = intent.getStringExtra("userEmail")

        println(userEmail)

        firstLogin = intent.getBooleanExtra("firstLogin",false)
        user = SharedPreferencesClass.getObject(applicationContext,userEmail!!)
        println("User basariyla cekildi mi")

        val degrees = listOf("Undergraduate", "Master's Degree", "PhD & semester")
        val adapter = ArrayAdapter(this, R.layout.list_item, degrees)
        val autoCompleteTextView = binding.educationalInformationAutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        //gerekli doldurulmus bilgileri otomatik giriyoruz.

        binding.accountTypeEditText.setText(user.accountType)
        if(user.studentID != "0"){ //kullanici student ise studentID gosterelim
            binding.studentIDEditText.setText(user.studentID)
        }
        else{ //kullanici Instructor ise studentID yok olsun
            binding.studentIDTextInputLayout.visibility = View.GONE
        }
        binding.nameEditText.setText(user.name)
        binding.surnameEditText.setText((user.surName))
        binding.emailEditText.setText(user.email)
        if(user.getEducationalInfo().isNotEmpty()){
            binding.educationalInformationAutoCompleteTextView.setText(user.getEducationalInfo())
        }
        if(user.getPhoneNumber().isNotEmpty()){
            binding.phoneNumberEditText.setText(user.getPhoneNumber())
        }
        if(user.getInstagramAdress().isNotEmpty()){
            binding.instagramEditText.setText(user.getInstagramAdress())
        }
        if(user.getTwitterAdress().isNotEmpty()){
            binding.twitterEditText.setText(user.getTwitterAdress())
        }
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

        if(user.getProfilePicture() != null){ //profil fotosu var ise goster.
            val bitmap = BitmapFactory.decodeByteArray(user.getProfilePicture(),0, user.getProfilePicture()!!.size)
            binding.profilePhoto.setImageBitmap(bitmap)
        }



        registerLaunchers()

    }

    //Buradan sonrasi tamamen izinlerle ilgili normal fonksiyonlar

    private fun openGallery(){
        val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intentGallery)
    }
    private fun openCamera() {
        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activityResultLauncher.launch(intentCamera)
    }
    private fun openGallery2(view : View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){ //android 13 ve ustu icin bunu yapmak zorundayiz.
            pendingAction = Runnable { openGallery() }
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }
                else{
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }


            } else {
                openGallery()
            }
        }else{//android 13 ve ustu degilse burayi yapiyoruz. read_external_storage
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                pendingAction = Runnable { openGallery() }
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }
                else{
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

    private fun registerLaunchers(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                val intentResult = result.data
                if(intentResult != null){
                    val image = intentResult.data
                    if(image != null) {
                        try {
                            if(Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(this@AccountInformation.contentResolver, image)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.profilePhoto.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,image)
                                binding.profilePhoto.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }else{
                        selectedBitmap = intentResult.extras?.get("data") as Bitmap?
                        binding.profilePhoto.setImageBitmap(selectedBitmap)
                    }
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
        val hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasGalleryPermission = ContextCompat.checkSelfPermission(this, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        when {
            hasCameraPermission && pendingAction === Runnable { openCamera() } -> {
                pendingAction?.run()
                pendingAction = null
            }
            hasGalleryPermission && pendingAction === Runnable { openGallery() } -> {
                pendingAction?.run()
                pendingAction = null
            }
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

    private fun smallerBitmap(image : Bitmap, maximumSize : Int) : Bitmap{ //bitmapi kaydederken kuculterek kaydediyoruz. Cok yer tutmasin
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble()/height.toDouble()
        if(bitmapRatio > 1){
            width = maximumSize
            val scaledHeight = width/bitmapRatio
            height = scaledHeight.toInt()
        }else{
            height = maximumSize
            val scaledWidth = height*bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width,height, true)
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
    fun saveInfo(view : View){ //save butonuna basildiginda neler olacak. Klasik kayit yapiyoruz.
        if(selectedBitmap != null){
            val smallBitmap = smallerBitmap(selectedBitmap!!,300)
            val output = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,output)
            val byteArray = output.toByteArray()
            user.setProfilePicture(byteArray)
        }
        if(checkInfo()){
            user.name = binding.nameEditText.text.toString()
            user.surName = binding.surnameEditText.text.toString()
            user.setEducationalInfo(binding.educationalInformationAutoCompleteTextView.text.toString())
            user.setPhoneNumber(binding.phoneNumberEditText.text.toString())
            user.setInstagramAdress(binding.instagramEditText.text.toString())
            user.setTwitterAdress(binding.twitterEditText.text.toString())
            SharedPreferencesClass.saveObject(applicationContext, user.email, user )
            if(firstLogin){
                val intent = Intent(this,HomeActivity::class.java)
                intent.putExtra("userEmail",user.email)
                startActivity(intent)
            }
            finish()
        }
        else{
            Toast.makeText(this,"Fill the all required fields",Toast.LENGTH_LONG).show()
        }
    }
}