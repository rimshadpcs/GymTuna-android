package com.rimapps.gymlog.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rimapps.gymlog.domain.model.WorkoutHistory
import com.rimapps.gymlog.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutHistoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WorkoutHistoryRepository {

    companion object {
        private const val TAG = "WorkoutHistoryRepo"
    }

    override suspend fun saveWorkoutHistory(workoutHistory: WorkoutHistory) {
        try {
            val userId = workoutHistory.userId
            val workoutId = workoutHistory.id

            Log.d(TAG, "Attempting to save workout history")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Workout ID: $workoutId")

            // Create the workout data map
            val workoutData = hashMapOf(
                "id" to workoutId,
                "userId" to userId,
                "name" to workoutHistory.name,
                "startTime" to workoutHistory.startTime,
                "endTime" to workoutHistory.endTime,
                "exercises" to workoutHistory.exercises.map { exercise ->
                    hashMapOf(
                        "exerciseId" to exercise.exerciseId,
                        "name" to exercise.name,
                        "notes" to exercise.notes,
                        "sets" to exercise.sets.map { set ->
                            hashMapOf(
                                "setNumber" to set.setNumber,
                                "weight" to set.weight,
                                "reps" to set.reps
                            )
                        }
                    )
                },
                "totalVolume" to workoutHistory.totalVolume,
                "totalSets" to workoutHistory.totalSets,
                "colorHex" to workoutHistory.colorHex,
                "routineId" to workoutHistory.routineId
            )

            // Direct path to workout document
            firestore
                .collection("workout_history")
                .document(userId)
                .collection("workouts")
                .document(workoutId)
                .set(workoutData)
                .await()

            Log.d(TAG, "Successfully saved workout history")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving workout history", e)
            Log.e(TAG, "Error details: ${e.message}")
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getMonthlyHistory(
        userId: String,
        monthStart: LocalDate,
        zone: ZoneId
    ): Flow<List<WorkoutHistory>> = callbackFlow {
        val startMs = monthStart
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val endMs = monthStart
            .plusMonths(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val listener = firestore
            .collection("workout_history")
            .document(userId)
            .collection("workouts")
            .where(Filter.greaterThanOrEqualTo("startTime", startMs))
            .where(Filter.lessThan("startTime", endMs))
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }

                val list = snap?.documents
                    ?.mapNotNull { it.toObject(WorkoutHistory::class.java) }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getWorkoutHistory(userId: String): Flow<List<WorkoutHistory>> =
        callbackFlow {
            val subscription = firestore
                .collection("workout_history")
                .document(userId)
                .collection("workouts")
                .orderBy("startTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        Log.e(TAG, "Error getting history", err)
                        return@addSnapshotListener
                    }

                    val list = snap?.documents
                        ?.mapNotNull { it.toObject(WorkoutHistory::class.java) }
                        ?: emptyList()

                    trySend(list)
                }

            awaitClose { subscription.remove() }
        }
}