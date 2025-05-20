package com.rimapps.gymlog.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.rimapps.gymlog.data.local.dao.ExerciseDao
import com.rimapps.gymlog.data.local.entity.ExerciseEntity
import com.rimapps.gymlog.data.local.entity.toExercise
import com.rimapps.gymlog.domain.model.Exercise
import com.rimapps.gymlog.domain.model.WeeklyCalendarDay
import com.rimapps.gymlog.domain.model.Workout
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import com.rimapps.gymlog.utils.RoutineColors
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
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
            .orderBy("createdAt")
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
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            userId = userId,
                            exercises = exercisesList,
                            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                            colorHex = data["colorHex"] as? String,
                            lastPerformed = data["lastPerformed"] as? Long
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
            val userId = authRepository.getCurrentUser()?.uid
                ?: throw Exception("User not authenticated")

            val doc = firestore.collection("user_workouts")
                .document(userId)
                .collection("routines")
                .document(workoutId)
                .get()
                .await()

            if (!doc.exists()) return null
            val data = doc.data ?: return null

            val exercises = (data["exercises"] as? List<Map<String, Any>>)
                ?.mapNotNull(::mapToExercise) ?: emptyList()

            Workout(
                id            = doc.id,
                name          = data["name"]        as? String ?: "",
                userId        = userId,
                exercises     = exercises,
                createdAt     = data["createdAt"]   as? Long   ?: System.currentTimeMillis(),
                colorHex      = data["colorHex"]    as? String,    // <- stays intact
                lastPerformed = data["lastPerformed"] as? Long
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching workout", e)
            null
        }
    }
    override suspend fun createWorkout(workout: Workout) {
        val userId = authRepository.getCurrentUser()?.uid
            ?: throw Exception("User not authenticated")

        val usedColours = firestore
            .collection("user_workouts")
            .document(userId)
            .collection("routines")
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("colorHex") }

        // 1-b  choose the first colour that is NOT in use
        val colorHex = workout.colorHex ?: run {
            RoutineColors.colorOptions
                .firstOrNull { it.hex !in usedColours }
                ?.hex
            // if all seven are taken, just cycle in order
                ?: RoutineColors.byIndex(usedColours.size)
        }

        val docId = workout.name
            .lowercase()
            .replace(" ", "_")
            .replace(Regex("[^a-z0-9_]"), "")

        firestore.collection("user_workouts")
            .document(userId)
            .collection("routines")
            .document(docId)
            .set(
                mapOf(
                    "name"          to workout.name,
                    "exercises"     to workout.exercises,
                    "createdAt"     to workout.createdAt,
                    "userId"        to userId,
                    "colorHex"      to colorHex,
                    "lastPerformed" to workout.lastPerformed
                )
            )
            .await()

        Log.d(TAG, "Created routine ${workout.name} with colour $colorHex")
    }


    override suspend fun updateWorkout(workout: Workout) {
        try {
            val userId = authRepository.getCurrentUser()?.uid ?: throw Exception("User not authenticated")
            Log.d(TAG, "Updating workout: ${workout.id} for user: $userId")

            // Map exercises to the format expected by Firestore
            val exercisesData = workout.exercises.map { exercise ->
                mapOf(
                    "id" to exercise.id,
                    "name" to exercise.name,
                    "equipment" to exercise.equipment,
                    "muscleGroup" to exercise.muscleGroup,
                    "defaultReps" to exercise.defaultReps,
                    "defaultSets" to exercise.defaultSets,
                    "isBodyweight" to exercise.isBodyweight,
                    "usesWeight" to exercise.usesWeight,
                    "description" to (exercise.description ?: "")
                )
            }

            // Create update data
            val updateData = mapOf(
                "name" to workout.name,
                "exercises" to exercisesData,
                "updatedAt" to System.currentTimeMillis(),
                "userId" to userId
            )

            // Update the document
            firestore.collection("user_workouts")
                .document(userId)
                .collection("routines")
                .document(workout.id)
                .set(updateData, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully updated workout: ${workout.name} with ${workout.exercises.size} exercises")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating workout", e)
            throw e
        }
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

    override suspend fun updateWorkoutColor(workoutId: String, colorHex: String) {
        try {
            val userId = authRepository.getCurrentUser()?.uid
                ?: throw Exception("User not authenticated")

            firestore.collection("user_workouts")
                .document(userId)
                .collection("routines")
                .document(workoutId)
                .update("colorHex", colorHex)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating workout color", e)
            throw e
        }
    }

    // ──────────────────────────────────────────────────────────────
// WorkoutRepositoryImpl.kt   (inside the class)
// ──────────────────────────────────────────────────────────────
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getWeeklyCalendar(): Flow<List<WeeklyCalendarDay>> = callbackFlow {

        val userId = authRepository.getCurrentUser()?.uid
            ?: throw IllegalStateException("User not authenticated")

        /* ---------- 1.  Build the list of dates for the current week ---------- */
        val zone       = ZoneId.systemDefault()
        val today      = LocalDate.now(zone)
        //val monday     = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        //val weekDates  = (0..6).map { monday.plusDays(it.toLong()) }
        val startDate  = today.minusDays(6)                   // ← 7-day window (today-6 … today)
        val weekDates  = (0..6).map { startDate.plusDays(it.toLong()) }

        fun emptyWeek() = weekDates.map { d -> WeeklyCalendarDay(d, null, false, null) }
        trySend(emptyWeek())                                          // emit blank immediately

        /* ---------- 2.  Listen for workout-history docs inside this week ------- */
        val startMs = startDate.atStartOfDay(zone).toInstant().toEpochMilli()

        val listener = firestore.collection("workout_history")
            .document(userId).collection("workouts")
            .where(Filter.greaterThanOrEqualTo("startTime", startMs))
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e(TAG, "getWeeklyCalendar: listener error", err)
                    return@addSnapshotListener
                }

                /* 2a) Map docs → info we need for the UI                            */
                val doneToday: Map<LocalDate, Triple<String?, Boolean, String?>> =
                    snap?.documents?.mapNotNull { d ->
                        val start = d.getLong("startTime") ?: return@mapNotNull null
                        val date  = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
                        val rid   = d.getString("routineId")
                        val hex   = d.getString("colorHex")        // we stored it, so never null
                        date to Triple(rid, true, hex)
                    }?.toMap() ?: emptyMap()

                /* 2b) Build visible calendar                                         */
                val calendar = weekDates.map { date ->
                    val triple = doneToday[date]
                    WeeklyCalendarDay(
                        date        = date,
                        routineId   = triple?.first,
                        isCompleted = triple?.second ?: false,
                        colorHex    = triple?.third
                    )
                }
                trySend(calendar)
            }

        awaitClose { listener.remove() }
    }


    override fun getSuggestedNextWorkout(): Flow<Workout?> = callbackFlow {
        val userId = authRepository.getCurrentUser()?.uid
            ?: throw Exception("User not authenticated")

        val listener = firestore.collection("user_workouts")
            .document(userId)
            .collection("routines")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching workouts", error)
                    return@addSnapshotListener
                }

                val workouts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Workout(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            userId = userId,
                            exercises = (data["exercises"] as? List<Map<String, Any>>)?.mapNotNull {
                                mapToExercise(it)
                            } ?: emptyList(),
                            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                            colorHex = data["colorHex"] as? String,
                            lastPerformed = data["lastPerformed"] as? Long
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping workout", e)
                        null
                    }
                } ?: emptyList()

                val suggested = workouts.minByOrNull { it.lastPerformed ?: 0 }
                trySend(suggested)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateWorkoutLastPerformed(routineId: String, lastPerformed: Long) {
        val userId = authRepository.getCurrentUser()?.uid
            ?: throw Exception("User not authenticated")

        firestore.collection("user_workouts")
            .document(userId)
            .collection("routines")
            .document(routineId)
            .update("lastPerformed", lastPerformed)
            .await()
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
