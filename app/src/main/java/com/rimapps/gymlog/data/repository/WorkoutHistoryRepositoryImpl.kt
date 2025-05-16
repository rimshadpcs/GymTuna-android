package com.rimapps.gymlog.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rimapps.gymlog.domain.model.WorkoutHistory
import com.rimapps.gymlog.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WorkoutHistoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WorkoutHistoryRepository {

    companion object {
        private const val TAG = "WorkoutHistoryRepo"
    }

    private val workoutHistoryCollection = firestore.collection("workout_history")

    override suspend fun saveWorkoutHistory(workoutHistory: WorkoutHistory) {
        try {
            Log.d(TAG, "Saving workout history: ${workoutHistory.id}")
            workoutHistoryCollection
                .document(workoutHistory.id)
                .set(workoutHistory)
                .await()
            Log.d(TAG, "Successfully saved workout history")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving workout history", e)
            throw e
        }
    }

    override suspend fun getWorkoutHistory(userId: String): Flow<List<WorkoutHistory>> = callbackFlow {
        Log.d(TAG, "Getting workout history for user: $userId")

        val subscription = workoutHistoryCollection
            .whereEqualTo("userId", userId)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting workout history", error)
                    return@addSnapshotListener
                }

                val workouts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(WorkoutHistory::class.java)
                } ?: emptyList()

                Log.d(TAG, "Retrieved ${workouts.size} workouts")
                trySend(workouts)
            }

        awaitClose {
            Log.d(TAG, "Closing workout history listener")
            subscription.remove()
        }
    }
}