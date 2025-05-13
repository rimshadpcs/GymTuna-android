package com.rimapps.gymlog

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp
@HiltAndroidApp
class GymLogApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        Firebase.initialize(this)

        // Debug Firestore
        val db = Firebase.firestore
        Log.d("GymLogApp", "🚀 Checking Firestore connection...")

        db.collection("workouts")
            .get()
            .addOnSuccessListener { result ->
                Log.d("GymLogApp", """
                    ✅ FIRESTORE CONNECTION SUCCESS
                    📊 Documents found: ${result.size()}
                """.trimIndent())

                result.forEach { document ->
                    Log.d("GymLogApp", """
                        📄 Document: ${document.id}
                        📝 Data: ${document.data}
                    """.trimIndent())
                }
            }
            .addOnFailureListener { e ->
                Log.e("GymLogApp", "❌ FIRESTORE CONNECTION FAILED", e)
                Log.e("GymLogApp", "Error message: ${e.message}")
                e.printStackTrace()
            }
    }
}