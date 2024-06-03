package com.huseyinemreseyrek.courseassistantapp
// <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/> Manifeste ekle
// <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
//<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
//<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
// add attandence button to home activity
// <activity
//            android:name=".AttendanceStudent"
//            android:exported="false" /> manifeste ekle
// no_connection.png  (daha düzgün resim bul)
//valid wifi adını değiştir

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.huseyinemreseyrek.courseassistantapp.databinding.ActivityAttendanceStudentBinding
import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.net.NetworkCapabilities
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.view.View
import android.location.LocationManager
import android.app.AlertDialog
import android.content.ContentValues
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.core.app.PendingIntentCompat.send
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
data class LatLng(val latitude: Double, val longitude: Double)
class AttendanceStudent : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var database: FirebaseDatabase
    private lateinit var courseRef: DatabaseReference
    private lateinit var binding: ActivityAttendanceStudentBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: String
    private lateinit var user: Person
    private lateinit var currCourseNames: ArrayList<String>
    private lateinit var currCourseInst: ArrayList<String>
    private lateinit var currCourseGroup: ArrayList<String>
    private var index: Int = 0
    private var wifiName: String? = ""
    private var validWifiName = "\"EDUROAM\""
    private var stdId: String = ""
    private var ATTENDANCE_PERMISSION_THRESHOLD_MINUTES:Long = 10
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceStudentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Ekrana Toast mesajları ekleyerek hata ayıklama
        //doToast("setContentView called")

        db = Firebase.firestore
        userEmail = intent.getStringExtra("userEmail") ?: run {
            doToast("No userEmail found in Intent")
            return
        }

        //doToast("intent received with userEmail: $userEmail")
        auth = Firebase.auth

        //doToast("FirebaseAuth initialized")

        getData()

        var dummy: Boolean = checkWifiAndLocation()

        currCourseNames = ArrayList()
        currCourseInst = ArrayList()
        currCourseGroup = ArrayList()
        db.collection("Students").document(userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ContentValues.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val registeredCourses =
                        snapshot.data?.get("RegisteredCourses") as? Map<String, Map<String, Any>>
                    if (registeredCourses != null) {
                        for ((courseName, courseDetails) in registeredCourses) {
                            currCourseNames.add(courseDetails["courseName"] as String)
                            currCourseInst.add(courseDetails["mainInstructor"] as String)
                            currCourseGroup.add(courseDetails["group"] as String)
                            index++
                        }
                    }
                }
            }
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, currCourseNames)
        binding.courseNameListView.adapter = adapter

        binding.courseNameListView.setOnItemClickListener { parent, view, position, id ->
            val selectedCourseName = parent.getItemAtPosition(position).toString()
            binding.courseNameEditText.setText(selectedCourseName)
            binding.courseNameListView.visibility = View.GONE
        }
        database = FirebaseDatabase.getInstance()
        courseRef = database.getReference("Attendance")
    }
    private fun checkWifiAndLocation() : Boolean{
        if (isWifiConnected() && isLocationAvailable()) {
            wifiName = getConnectedWifiName()
            if(wifiName == validWifiName) {
                binding.tryAgainButton.isClickable = false
                binding.tryAgainButton.visibility = View.INVISIBLE
                binding.connection.visibility = View.INVISIBLE

                binding.sendAttendance.isClickable = true;
                binding.sendAttendance.visibility = View.VISIBLE
                binding.courseNameEditText.isClickable = true
                binding.courseNameEditText.visibility = View.VISIBLE
                binding.CourseNameTextInputLayout.isClickable = true
                binding.CourseNameTextInputLayout.visibility = View.VISIBLE
                return true
            }
            else{
                binding.sendAttendance.isClickable = false
                binding.sendAttendance.visibility = View.INVISIBLE
                binding.courseNameEditText.isClickable = false
                binding.courseNameEditText.visibility = View.INVISIBLE
                binding.CourseNameTextInputLayout.isClickable = false
                binding.CourseNameTextInputLayout.visibility = View.INVISIBLE

                binding.connection.visibility = View.VISIBLE
                binding.tryAgainButton.visibility = View.VISIBLE
                binding.tryAgainButton.isClickable = true
                popUpMessage("Warning", "Please make sure that you are connected to the school wifi network, your location is turned on and you allow location access.")
            }
        }
        else{
            binding.sendAttendance.isClickable = false
            binding.sendAttendance.visibility = View.INVISIBLE
            binding.courseNameEditText.isClickable = false
            binding.courseNameEditText.visibility = View.INVISIBLE
            binding.CourseNameTextInputLayout.isClickable = false
            binding.CourseNameTextInputLayout.visibility = View.INVISIBLE

            binding.connection.visibility = View.VISIBLE
            binding.tryAgainButton.visibility = View.VISIBLE
            binding.tryAgainButton.isClickable = true
            popUpMessage("Warning", "Please make sure that you are connected to the school wifi network, your location is turned on and you allow location access.")
        }
        return false
    }
    private fun isLocationAvailable(): Boolean {
        if (!checkLocationPermission()) {
            requestLocationPermission()
        }
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGpsEnabled || isNetworkEnabled
    }
    private fun checkLocationPermission(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }
    private fun isWifiConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    private fun getConnectedWifiName(): String? {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connInfo = wifiManager.connectionInfo
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            connInfo.ssid
        } else {
            connInfo.ssid.trim { it == '"' }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    wifiName = getConnectedWifiName()
                }
            }
        }
    }
    private fun getData() {
        println("Getting data for $userEmail")
        val docRef = db.collection("Students").document(userEmail)
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
                val studentID = documentSnapshot.getString("studentID") ?: "N/A"
                stdId = studentID
                user = Student(accountType, studentID, name, surname, email, downloadUrl, educationalInfo, phoneNumber, instagramAddress, twitterAddress, currentCourses)
            } else {
                println("Document does not exist.")
            }
        }.addOnFailureListener { e ->
            println("Error fetching document: ${e.localizedMessage}")
        }
    }

    fun Context.doToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun tryConnection(view: View) {
        var dummy:Boolean = checkWifiAndLocation()
    }

    @SuppressLint("NewApi")
    fun sendAttendanceInfo(view: View) {
        val courseName: String = binding.courseNameEditText.text.toString()
        val i: Int = currCourseNames.indexOf(courseName)
        if(checkWifiAndLocation()){
            CoroutineScope(Dispatchers.Main).launch {
                val isPossible:Boolean = checkInstructorPermission(currCourseInst[i], currCourseGroup[i])
                if (isPossible){
                    CoroutineScope(Dispatchers.Main).launch {
                        val isInDepartment:Boolean = checkLocation()
                        if(isInDepartment){
                            doToast("Sending...")
                            val currentDateTime = LocalDateTime.now()
                            val date = currentDateTime.toString().substring(0,10)
                            val time = currentDateTime.toString().substring(11,19)
                            val attendanceData = hashMapOf(
                                "courseName" to courseName,
                                "group" to currCourseGroup[i],
                                "date" to date,
                                "studentID" to stdId,
                                "time" to time
                            )
                            CoroutineScope(Dispatchers.Main).launch {
                                val isSuccessful = send(currCourseInst[i], attendanceData)
                                if (!isSuccessful) {
                                    doToast("Unsuccessful")
                                }
                            }
                        }
                        else{
                            doToast("Your location is not in department area")
                        }
                    }

                }
                else{
                    doToast("Please wait for your instructor to activate attendance.")
                }
            }
        }
    }
    @SuppressLint("NewApi")
    suspend fun checkInstructorPermission(instructorMail: String, courseGroup:String): Boolean {
        val courseName: String = binding.courseNameEditText.text.toString()
        return suspendCancellableCoroutine { continuation ->
            val permissionCollectionRef = db.collection("AttendancePermissions").document(instructorMail).collection("Permissions")
            permissionCollectionRef
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        for (document in documents) {
                            val permissionData = document.data
                            val permissionTime = permissionData?.get("time") as? String
                            val name = permissionData?.get("courseName") as? String
                            val group = permissionData?.get("group") as? String
                            if(courseName == name && (group == "Founder" || group == courseGroup)){
                                val currentDateTime = LocalDateTime.now()
                                val currentTime = currentDateTime.toString().substring(11,19)
                                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                                val currentLocalTime = LocalTime.parse(currentTime, formatter)
                                val permissionLocalTime = LocalTime.parse(permissionTime, formatter)
                                val differenceInMinutes = permissionLocalTime.until(currentLocalTime, java.time.temporal.ChronoUnit.MINUTES)
                                if(differenceInMinutes > ATTENDANCE_PERMISSION_THRESHOLD_MINUTES){
                                    doToast("The attendance period has expired.")
                                    continuation.resume(false)
                                    return@addOnSuccessListener
                                }
                                continuation.resume(true)
                                return@addOnSuccessListener
                            }
                        }
                        continuation.resume(false)
                        return@addOnSuccessListener
                    } else {
                        continuation.resume(false)
                        return@addOnSuccessListener
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(false)
                    doToast("Error: ${e.message}")
                }
        }
    }
    suspend fun checkLocation(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                doToast("Please give location permission")
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        val isInDesignatedArea = isWithinDesignatedArea(location)
                        continuation.resume(isInDesignatedArea)
                    } else {
                        continuation.resume(false)
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                        continuation.resume(false)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
    private fun isWithinDesignatedArea(location: Location): Boolean {
        val lat = location.latitude
        val lng = location.longitude

        //val topLeft = LatLng(41.028655, 28.891026)
        //val topRight = LatLng(41.028301, 28.891377)
        //val bottomRight = LatLng(41.027773, 28.890663)
        //val bottomLeft = LatLng(41.028015, 28.889972)

        val topLeft = LatLng(41.028514, 28.890980)
        val topRight = LatLng(41.028336, 28.891222)
        val bottomRight = LatLng(41.028018, 28.890638)
        val bottomLeft = LatLng(41.028183, 28.890285)

        val minLat = minOf(topLeft.latitude, topRight.latitude, bottomRight.latitude, bottomLeft.latitude)
        val maxLat = maxOf(topLeft.latitude, topRight.latitude, bottomRight.latitude, bottomLeft.latitude)
        val minLng = minOf(topLeft.longitude, topRight.longitude, bottomRight.longitude, bottomLeft.longitude)
        val maxLng = maxOf(topLeft.longitude, topRight.longitude, bottomRight.longitude, bottomLeft.longitude)

        return lat in minLat..maxLat && lng in minLng..maxLng
    }
    suspend fun send(instMail: String, data: Map<String, Any>): Boolean {
        val db = FirebaseFirestore.getInstance()
        return suspendCancellableCoroutine { continuation ->
            val attendanceCollection = db.collection("Attendance").document(instMail).collection("Attendances")
            attendanceCollection.add(data)
                .addOnSuccessListener {
                    doToast("Successful")
                    continuation.resume(true)
                }
                .addOnFailureListener { e ->
                    continuation.resume(false)
                }
        }
    }
    fun makeListVisible(view: View) {
        binding.courseNameListView.visibility = View.VISIBLE
    }
    private fun popUpMessage(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Okay") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}




