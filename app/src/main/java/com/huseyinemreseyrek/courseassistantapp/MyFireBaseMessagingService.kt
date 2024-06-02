// MyFirebaseMessagingService.kt
package com.huseyinemreseyrek.courseassistantapp

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Message data payload: " + remoteMessage.data)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val userEmail = Firebase.auth.currentUser?.email ?: return
        val db = Firebase.firestore
        val userDocRef = db.collection("Students").document(userEmail)
        userDocRef.update("token", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token successfully saved")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error saving token", e)
            }
    }
}
