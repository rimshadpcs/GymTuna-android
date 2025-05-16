package com.rimapps.gymlog.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rimapps.gymlog.data.local.dao.ExerciseDao
import com.rimapps.gymlog.data.local.entity.ExerciseEntity
import com.rimapps.gymlog.data.local.entity.toExercise
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.Workout
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WorkoutRepository"

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val exerciseDao: ExerciseDao,
    private val authRepository: AuthRepository
) : WorkoutRepository {

    companion object {
        private const val TAG = "WorkoutRepository"
    }

    private val workoutsCollection = firestore.collection("user_workouts")
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
        val subscription = firestore.collection("user_workouts")
            .document(userId)
            .collection("routines")
            .orderBy("createdAt") // Order by name instead of createdAt
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching workouts", error)
                    return@addSnapshotListener
                }

                val workouts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null

                        // Map exercises list
                        val exercisesList = (data["exercises"] as? List<Map<String, Any>>)?.mapNotNull { exerciseMap ->
                            Exercise(
                                id = exerciseMap["id"] as? String ?: "",
                                name = exerciseMap["name"] as? String ?: return@mapNotNull null,
                                equipment = exerciseMap["equipment"] as? String ?: "",
                                muscleGroup = exerciseMap["muscleGroup"] as? String ?: "",
                                defaultReps = (exerciseMap["defaultReps"] as? Long)?.toInt() ?: 15,
                                defaultSets = (exerciseMap["defaultSets"] as? Long)?.toInt() ?: 3,
                                isBodyweight = exerciseMap["isBodyweight"] as? Boolean ?: false,
                                usesWeight = exerciseMap["usesWeight"] as? Boolean ?: true,
                                description = exerciseMap["description"] as? String ?: ""
                            )
                        } ?: emptyList()

                        Workout(
                            id = doc.id, // This will now be the formatted name
                            name = data["name"] as? String ?: "",
                            userId = userId,
                            exercises = exercisesList,
                            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping workout document", e)
                        null
                    }
                } ?: emptyList()

                trySend(workouts)
            }

        awaitClose { subscription.remove() }
    }


    override suspend fun getWorkoutById(workoutId: String): Workout? {
        return try {
            val userId =
                authRepository.getCurrentUser()?.uid ?: throw Exception("User not authenticated")
            Log.d(TAG, "Fetching workout with ID: $workoutId for user: $userId")

            val document = firestore.collection("user_workouts")
                .document(userId)
                .collection("routines")
                .document(workoutId)
                .get()
                .await()

            if (!document.exists()) {
                Log.d(TAG, "No workout found with ID: $workoutId")
                return null
            }

            val data = document.data
            if (data == null) {
                Log.d(TAG, "Workout data is null for ID: $workoutId")
                return null
            }

            // Map the exercises field
            val exercisesList =
                (data["exercises"] as? List<Map<String, Any>>)?.mapNotNull { exerciseMap ->
                    try {
                        Exercise(
                            id = exerciseMap["id"] as? String ?: "",
                            name = exerciseMap["name"] as? String ?: return@mapNotNull null,
                            equipment = exerciseMap["equipment"] as? String ?: "",
                            muscleGroup = exerciseMap["muscleGroup"] as? String ?: "",
                            defaultReps = (exerciseMap["defaultReps"] as? Long)?.toInt() ?: 15,
                            defaultSets = (exerciseMap["defaultSets"] as? Long)?.toInt() ?: 3,
                            isBodyweight = exerciseMap["isBodyweight"] as? Boolean ?: false,
                            usesWeight = exerciseMap["usesWeight"] as? Boolean ?: true,
                            description = exerciseMap["description"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping exercise", e)
                        null
                    }
                } ?: emptyList()

            Log.d(TAG, "Successfully mapped ${exercisesList.size} exercises")

            Workout(
                id = document.id,
                name = data["name"] as? String ?: "",
                userId = userId,
                exercises = exercisesList,
                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
            ).also {
                Log.d(TAG, "Created workout object: ${it.name} with ${it.exercises.size} exercises")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching workout", e)
            null
        }
    }


    override suspend fun createWorkout(workout: Workout) {
        try {
            val userId = authRepository.getCurrentUser()?.uid ?: throw Exception("User not authenticated")

            // Format the document ID from the workout name
            val docId = workout.name.toLowerCase()
                .replace(" ", "_")
                .replace(Regex("[^a-z0-9_]"), "") // Remove any special characters

            // Create the workout document in the user's routines subcollection
            firestore.collection("user_workouts")
                .document(userId)
                .collection("routines")
                .document(docId) // Use the formatted name as document ID
                .set(
                    mapOf(
                        "name" to workout.name,
                        "exercises" to workout.exercises,
                        "createdAt" to workout.createdAt,
                        "userId" to userId
                    )
                )
                .await()


            Log.d(TAG, "Successfully created workout: ${workout.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating workout", e)
            throw e
        }
    }
    override suspend fun updateWorkout(workout: Workout) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWorkout(workoutId: String) {
        try {
            val userId = authRepository.getCurrentUser()?.uid ?: throw Exception("User not authenticated")

            firestore.collection("user_workouts")
                .document(userId)
                .collection("routines")
                .document(workoutId)
                .delete()
                .await()

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting workout", e)
            throw e
        }
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