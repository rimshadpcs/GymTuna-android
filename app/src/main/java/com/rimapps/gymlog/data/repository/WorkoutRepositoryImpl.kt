package com.rimapps.gymlog.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.rimapps.gymlog.data.local.dao.ExerciseDao
import com.rimapps.gymlog.data.local.entity.ExerciseEntity
import com.rimapps.gymlog.data.local.entity.toExercise
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.Workout
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WorkoutRepository"

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val exerciseDao: ExerciseDao
) : WorkoutRepository {
    init {
        Log.d(TAG, "⭐ GymLogApp Initialization Started ⭐")

        // Immediate Firestore check
        firestore.collection("workouts")
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d(TAG, "🔥 FIRESTORE CHECK 🔥")
                Log.d(TAG, "Documents count: ${snapshot.size()}")

                if (snapshot.isEmpty) {
                    Log.d(TAG, "❌ NO DOCUMENTS FOUND IN FIRESTORE")
                } else {
                    snapshot.documents.forEach { doc ->
                        Log.d(TAG, """
                        📄 Document: ${doc.id}
                        📝 Raw Data: ${doc.data}
                    """.trimIndent())
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ FIRESTORE CHECK FAILED", e)
            }

        // Also check Firestore connection
        firestore.enableNetwork()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Firestore network enabled")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Firestore network enable failed", e)
            }
    }
    init {
        Log.d(TAG, "Initializing WorkoutRepositoryImpl")
        syncExercises()
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun syncExercises() {
        Log.d(TAG, "Starting exercise sync")

        // First do a direct get to verify data
        firestore.collection("workouts").get()
            .addOnSuccessListener { snapshot ->
                Log.d(TAG, "=== FIRESTORE DIRECT CHECK ===")
                Log.d(TAG, "Documents found: ${snapshot.size()}")
                snapshot.documents.forEach { doc ->
                    Log.d(TAG, """
                    Document ${doc.id}:
                    Data: ${doc.data}
                    Fields:
                    - name: ${doc.getString("name")}
                    - equipment: ${doc.getString("equipment")}
                    - muscleGroup: ${doc.getString("muscleGroup")}
                    - defaultReps: ${doc.getLong("defaultReps")}
                    - defaultSets: ${doc.getLong("defaultSets")}
                    - isBodyweight: ${doc.getBoolean("isBodyweight")}
                    - usesWeight: ${doc.getBoolean("usesWeight")}
                """.trimIndent())
                }

                Log.d(TAG, "Direct query result - Document count: ${snapshot.size()}")
                snapshot.documents.forEach { doc ->
                    Log.d(TAG, "Document ID: ${doc.id}")
                    Log.d(TAG, "Document data: ${doc.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Direct query failed", e)
            }

        // Then set up the listener
        firestore.collection("workouts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error syncing exercises", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.d(TAG, "No exercises snapshot")
                    return@addSnapshotListener
                }

                Log.d(TAG, "Received ${snapshot.documents.size} exercises from Firestore")

                snapshot.documents.forEach { doc ->
                    Log.d(TAG, "Processing document: ${doc.id}")
                    Log.d(TAG, "Document data: ${doc.data}")
                }

                val exercises = snapshot.documents.mapNotNull { doc ->
                    try {
                        val name = doc.getString("name")
                        val equipment = doc.getString("equipment")
                        val muscleGroup = doc.getString("muscleGroup")
                        val defaultReps = doc.getLong("defaultReps")
                        val defaultSets = doc.getLong("defaultSets")
                        val isBodyweight = doc.getBoolean("isBodyweight")
                        val usesWeight = doc.getBoolean("usesWeight")

                        Log.d(TAG, """
                        Mapping exercise:
                        - name: $name
                        - equipment: $equipment
                        - muscleGroup: $muscleGroup
                        - defaultReps: $defaultReps
                        - defaultSets: $defaultSets
                        - isBodyweight: $isBodyweight
                        - usesWeight: $usesWeight
                    """.trimIndent())

                        if (name == null) {
                            Log.e(TAG, "Exercise missing name: ${doc.id}")
                            return@mapNotNull null
                        }

                        ExerciseEntity(
                            name = name,
                            equipment = equipment ?: "",
                            muscleGroup = muscleGroup ?: "",
                            defaultReps = defaultReps?.toInt() ?: 15,
                            defaultSets = defaultSets?.toInt() ?: 3,
                            isBodyweight = isBodyweight ?: false,
                            usesWeight = usesWeight ?: true
                        ).also {
                            Log.d(TAG, "Successfully mapped exercise: ${it.name}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping exercise doc: ${doc.id}", e)
                        e.printStackTrace()
                        null
                    }
                }

                Log.d(TAG, "Mapped ${exercises.size} exercises, attempting Room insert")

                kotlinx.coroutines.GlobalScope.launch {
                    try {
                        val beforeCount = exerciseDao.getExerciseCount()
                        Log.d(TAG, "Room count before clear: $beforeCount")

                        exerciseDao.clearExercises()
                        Log.d(TAG, "Cleared exercises from Room")

                        exerciseDao.insertExercises(exercises)
                        Log.d(TAG, "Inserted new exercises into Room")

                        val afterCount = exerciseDao.getExerciseCount()
                        Log.d(TAG, "Room count after insert: $afterCount")

                        // Verify what was actually inserted
                        exerciseDao.getAllExercises().collect { entities ->
                            Log.d(TAG, "Verifying inserted exercises:")
                            entities.forEach { entity ->
                                Log.d(TAG, "Stored exercise: ${entity.name}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error working with Room database", e)
                        e.printStackTrace()
                    }
                }
            }
    }

    override suspend fun getWorkouts(userId: String): Flow<List<Workout>> = callbackFlow {
        // This is where the issue is - it's automatically creating workouts from exercises
        // Let's modify this to only return user-created workouts

        val subscription = firestore.collection("user_workouts")
            .document(userId)
            .collection("routines")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting workouts", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val workouts = snapshot.documents.mapNotNull { doc ->
                    try {
                        val exercisesList = doc.get("exercises") as? List<Map<String, Any>> ?: emptyList()

                        val exercises = exercisesList.mapNotNull { exerciseMap ->
                            try {
                                Exercise(
                                    name = exerciseMap["name"] as? String ?: return@mapNotNull null,
                                    equipment = exerciseMap["equipment"] as? String ?: "",
                                    muscleGroup = exerciseMap["muscleGroup"] as? String ?: "",
                                    defaultReps = (exerciseMap["defaultReps"] as? Long)?.toInt() ?: 15,
                                    defaultSets = (exerciseMap["defaultSets"] as? Long)?.toInt() ?: 3,
                                    isBodyweight = exerciseMap["isBodyweight"] as? Boolean ?: false,
                                    usesWeight = exerciseMap["usesWeight"] as? Boolean ?: true
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error mapping exercise in workout", e)
                                null
                            }
                        }

                        Workout(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unnamed Workout",
                            exercises = exercises,
                            userId = userId,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping workout", e)
                        null
                    }
                }

                Log.d(TAG, "Sending ${workouts.size} user workouts")
                trySend(workouts)
            }

        awaitClose { subscription.remove() }
    }


    override suspend fun createWorkout(workout: Workout) {
        val workoutData = hashMapOf(
            "name" to workout.name,
            "userId" to workout.userId,
            "createdAt" to workout.createdAt,
            "exercises" to workout.exercises.map { exercise ->
                hashMapOf(
                    "name" to exercise.name,
                    "equipment" to exercise.equipment,
                    "muscleGroup" to exercise.muscleGroup,
                    "defaultReps" to exercise.defaultReps,
                    "defaultSets" to exercise.defaultSets,
                    "isBodyweight" to exercise.isBodyweight,
                    "usesWeight" to exercise.usesWeight
                )
            }
        )

        firestore.collection("user_workouts")
            .document(workout.userId)
            .collection("routines")
            .document(workout.id)
            .set(workoutData)
    }
    override suspend fun updateWorkout(workout: Workout) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWorkout(workoutId: String) {
        TODO("Not yet implemented")
    }

    override fun searchExercises(query: String): Flow<List<Exercise>> {
        Log.d(TAG, "Searching exercises with query: '$query'")
        kotlinx.coroutines.GlobalScope.launch {
            val count = exerciseDao.getExerciseCount()
            val names = exerciseDao.getAllExerciseNames()
            Log.d(TAG, "Current database state:")
            Log.d(TAG, "- Total count: $count")
            Log.d(TAG, "- Available names: $names")
        }

        return exerciseDao.searchExercises(query)
            .catch { e ->
                Log.e(TAG, "Error searching exercises", e)
                e.printStackTrace()
                emit(emptyList())
            }
            .map { entities ->
                entities.map { it.toExercise() }
                    .also { Log.d(TAG, "Search results for '$query': ${it.size} exercises") }
            }
    }

    override suspend fun getExercises(): Flow<List<Exercise>> {
        Log.d(TAG, "Getting all exercises")
        kotlinx.coroutines.GlobalScope.launch {
            val count = exerciseDao.getExerciseCount()
            val firstExercise = exerciseDao.getFirstExercise()
            Log.d(TAG, """
            Database state check:
            - Total count: $count
            - First exercise: ${firstExercise?.name ?: "none"}
        """.trimIndent())
        }

        return exerciseDao.getAllExercises()
            .catch { e ->
                Log.e(TAG, "Error getting all exercises", e)
                e.printStackTrace()
                emit(emptyList())
            }
            .map { entities ->
                entities.map { it.toExercise() }
                    .also { Log.d(TAG, "Got ${it.size} exercises from Room") }
            }
    }
}