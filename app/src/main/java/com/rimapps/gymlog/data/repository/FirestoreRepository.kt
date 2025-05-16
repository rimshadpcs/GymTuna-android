package com.rimapps.gymlog.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.UserProfile
import com.rimapps.gymlog.domain.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val workoutsCollection = firestore.collection("user_workouts")
    private val exercisesCollection = firestore.collection("exercises")

    companion object {
        private const val TAG = "FirestoreRepository"
        private const val USERS_COLLECTION = "users"
        private const val USER_STATS_COLLECTION = "user_stats"
    }

    suspend fun createOrUpdateUser(userProfile: UserProfile) {
        try {
            Log.d(TAG, "Creating/updating user profile for uid: ${userProfile.uid}")
            withContext(Dispatchers.IO) {
                // First, check if user document exists
                val userDoc = firestore.collection(USERS_COLLECTION)
                    .document(userProfile.uid)
                    .get()
                    .await()

                if (!userDoc.exists()) {
                    Log.d(TAG, "User document doesn't exist, creating new profile")
                    // Create new profile
                    firestore.collection(USERS_COLLECTION)
                        .document(userProfile.uid)
                        .set(userProfile)
                        .await()

                    // Create initial stats
                    firestore.collection(USERS_COLLECTION)
                        .document(userProfile.uid)
                        .collection(USER_STATS_COLLECTION)
                        .document("stats")
                        .set(mapOf(
                            "totalWorkouts" to 0,
                            "totalVolume" to 0.0,
                            "lastWorkoutDate" to null,
                            "createdAt" to System.currentTimeMillis()
                        ))
                        .await()
                    Log.d(TAG, "Successfully created new user profile and stats")
                } else {
                    Log.d(TAG, "Updating existing user profile")
                    // Update last login
                    firestore.collection(USERS_COLLECTION)
                        .document(userProfile.uid)
                        .set(
                            mapOf("lastLoginAt" to System.currentTimeMillis()),
                            SetOptions.merge()
                        )
                        .await()
                    Log.d(TAG, "Successfully updated user profile")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in createOrUpdateUser", e)
            throw Exception("Failed to create/update user profile: ${e.localizedMessage}")
        }
    }

    suspend fun getWorkoutById(workoutId: String): Workout? {
        return try {
            val document = workoutsCollection.document(workoutId).get().await()
            val data = document.data ?: return null

            // Map the exercises field
            val exercisesList = (data["exercises"] as? List<Map<String, Any>>)?.mapNotNull { exerciseMap ->
                mapToExercise(exerciseMap)
            } ?: emptyList()

            Workout(
                id = document.id,
                name = data["name"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                exercises = exercisesList,
                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun mapToExercise(exerciseMap: Map<String, Any>): Exercise? {
        return try {
            Exercise(
                id = exerciseMap["id"] as? String ?: "",
                name = exerciseMap["name"] as? String ?: return null,
                equipment = exerciseMap["equipment"] as? String ?: "",
                muscleGroup = exerciseMap["muscleGroup"] as? String ?: "",
                defaultReps = (exerciseMap["defaultReps"] as? Long)?.toInt() ?: 15,
                defaultSets = (exerciseMap["defaultSets"] as? Long)?.toInt() ?: 3,
                isBodyweight = exerciseMap["isBodyweight"] as? Boolean ?: false,
                usesWeight = exerciseMap["usesWeight"] as? Boolean ?: true,
                description = exerciseMap["description"] as? String ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}