package com.rimapps.gymlog.presentation.workoutScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rimapps.gymlog.domain.model.*
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutHistoryRepository
import com.rimapps.gymlog.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.google.firebase.firestore.Query        // ← for orderBy
import kotlinx.coroutines.tasks.await
private const val TAG = "WorkoutViewModel"

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val historyRepo:  WorkoutHistoryRepository,
    private val workoutRepository:  WorkoutRepository,
    private val authRepository:     AuthRepository,
    private val firestore   : FirebaseFirestore    // ← NEW
) : ViewModel() {

    /* ──────────────────────────────────── timers & flags ───────────────────────────────────── */
    private var startMs         = System.currentTimeMillis()
    private var isWorkoutActive = true          // killed in onCleared / discardWorkout()

    /* ──────────────────────────────────── routine meta ─────────────────────────────────────── */
    private var currentRoutineId:   String? = null
    private var originalRoutineName: String? = null     // unchanged name for history doc
    private var isRoutineModified   = false             // flag to trigger "update routine" dialog
    private var isInitialized       = false

    /* ──────────────────────────────────── UI state flows ───────────────────────────────────── */
    private val _workoutDuration     = MutableStateFlow("0s")
    val  workoutDuration: StateFlow<String> = _workoutDuration.asStateFlow()

    private val _routineName         = MutableStateFlow<String?>(null)
    val  routineName:   StateFlow<String?> = _routineName.asStateFlow()

    private val _totalVolume         = MutableStateFlow(0)         // kg
    val  totalVolume:   StateFlow<Int> = _totalVolume.asStateFlow()

    private val _totalSets           = MutableStateFlow(0)
    val  totalSets:     StateFlow<Int> = _totalSets.asStateFlow()

    private val _exercises           = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val  exercises:      StateFlow<List<WorkoutExercise>> = _exercises.asStateFlow()

    private val _workoutState        = MutableStateFlow<ActiveWorkoutState>(ActiveWorkoutState.Initial)
    val  workoutState:  StateFlow<ActiveWorkoutState> = _workoutState.asStateFlow()

    private val _showUpdateRoutineDialog = MutableStateFlow(false)
    val  showUpdateRoutineDialog: StateFlow<Boolean> = _showUpdateRoutineDialog.asStateFlow()

    init { startTimer() }


    /**
     * Helper function to find previous and best sets for an exercise
     * @param exerciseId ID of the exercise to find sets for
     * @return Map of set numbers to Pair(lastSet, bestSet)
     */
    private suspend fun findPreviousAndBestSets(exerciseId: String): Map<Int, Pair<CompletedSet?, CompletedSet?>> {
        val result = mutableMapOf<Int, Pair<CompletedSet?, CompletedSet?>>();
        try {
            val uid = authRepository.getCurrentUser()?.uid ?: return result
            
            // DIRECT FIRESTORE QUERY WITH LOGGING
            Log.d(TAG, "DIRECT QUERY: Finding history for exercise: $exerciseId for user: $uid")
            
            // Get workout history documents directly
            val allHistory = firestore.collection("workout_history")
                .document(uid)
                .collection("workouts")
                .orderBy("endTime", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            
            Log.d(TAG, "FOUND ${allHistory.documents.size} TOTAL HISTORY DOCUMENTS")
            
            // Process documents directly without converting to model objects
            val workoutsWithExercise = mutableListOf<Map<String, Any>>()
            
            for (doc in allHistory.documents) {
                try {
                    val data = doc.data
                    if (data != null) {
                        // Get exercises array from the document
                        @Suppress("UNCHECKED_CAST")
                        val exercises = data["exercises"] as? List<Map<String, Any>> ?: continue
                        
                        // Check if any exercise matches our target exerciseId or name
                        val matchingExercises = exercises.filter { ex ->
                            val exId = ex["exerciseId"] as? String ?: ""
                            val exName = ex["name"] as? String ?: ""
                            
                            exId == exerciseId || 
                            (exId.isEmpty() && exName.equals(exerciseId, ignoreCase = true))
                        }
                        
                        if (matchingExercises.isNotEmpty()) {
                            Log.d(TAG, "FOUND MATCHING EXERCISE in doc ${doc.id}")
                            workoutsWithExercise.add(data)
                            
                            // Log matching exercise details
                            matchingExercises.forEach { exercise ->
                                val exName = exercise["name"] as? String ?: ""
                                @Suppress("UNCHECKED_CAST")
                                val sets = exercise["sets"] as? List<Map<String, Any>> ?: emptyList()
                                
                                Log.d(TAG, "  Exercise: $exName with ${sets.size} sets")
                                sets.forEach { set ->
                                    val setNum = set["setNumber"] as? Long ?: 0
                                    val weight = set["weight"] as? Double ?: 0.0
                                    val reps = set["reps"] as? Long ?: 0
                                    Log.d(TAG, "    Set $setNum: ${weight}kg x $reps")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing document: ${doc.id}", e)
                }
            }
            
            Log.d(TAG, "FILTERED TO ${workoutsWithExercise.size} WORKOUTS WITH MATCHING EXERCISE")
            
            // If no matching workouts found, return empty result
            if (workoutsWithExercise.isEmpty()) {
                Log.d(TAG, "NO HISTORY FOUND FOR EXERCISE: $exerciseId")
                return result
            }
            
            // Process the most recent workout for PREVIOUS values
            val mostRecentWorkout = workoutsWithExercise.first()
            @Suppress("UNCHECKED_CAST")
            val exercises = mostRecentWorkout["exercises"] as? List<Map<String, Any>> ?: emptyList()
            
            // Find the matching exercise
            val matchingExercise = exercises.find { ex ->
                val exId = ex["exerciseId"] as? String ?: ""
                val exName = ex["name"] as? String ?: ""
                
                exId == exerciseId || 
                (exId.isEmpty() && exName.equals(exerciseId, ignoreCase = true))
            } ?: return result
            
            // Get sets from the matching exercise
            @Suppress("UNCHECKED_CAST")
            val previousSets = matchingExercise["sets"] as? List<Map<String, Any>> ?: emptyList()
            
            Log.d(TAG, "FOUND ${previousSets.size} SETS IN MOST RECENT WORKOUT")
            
            // Collect all sets from all matching exercises in all workouts
            val allSets = mutableListOf<Map<String, Any>>()
            
            workoutsWithExercise.forEach { workout ->
                @Suppress("UNCHECKED_CAST")
                val workoutExercises = workout["exercises"] as? List<Map<String, Any>> ?: emptyList()
                
                workoutExercises.forEach { ex ->
                    val exId = ex["exerciseId"] as? String ?: ""
                    val exName = ex["name"] as? String ?: ""
                    
                    if (exId == exerciseId || (exId.isEmpty() && exName.equals(exerciseId, ignoreCase = true))) {
                        @Suppress("UNCHECKED_CAST")
                        val sets = ex["sets"] as? List<Map<String, Any>> ?: emptyList()
                        allSets.addAll(sets)
                    }
                }
            }
            
            Log.d(TAG, "COLLECTED ${allSets.size} TOTAL SETS ACROSS ALL WORKOUTS")
            
            // Group sets by set number
            val setsByNumber = allSets.groupBy { set ->
                (set["setNumber"] as? Long)?.toInt() ?: 0
            }
            
            // Find best set for each position (highest weight×reps value)
            val bestSets = mutableMapOf<Int, Map<String, Any>>()
            
            setsByNumber.forEach { (setNum, sets) ->
                if (sets.isNotEmpty()) {
                    // Find the best set based on weight * reps
                    val bestSet = sets.maxByOrNull { set ->
                        val weight = set["weight"] as? Double ?: 0.0
                        val reps = (set["reps"] as? Long)?.toInt() ?: 0
                        weight * reps
                    }
                    
                    if (bestSet != null) {
                        bestSets[setNum] = bestSet
                        val weight = bestSet["weight"] as? Double ?: 0.0
                        val reps = (bestSet["reps"] as? Long)?.toInt() ?: 0
                        Log.d(TAG, "BEST FOR SET $setNum: ${weight}kg x $reps")
                    }
                }
            }
            
            // Process previous sets to build result map
            previousSets.forEach { prevSet ->
                val setNumber = (prevSet["setNumber"] as? Long)?.toInt() ?: 0
                val prevWeight = prevSet["weight"] as? Double ?: 0.0
                val prevReps = (prevSet["reps"] as? Long)?.toInt() ?: 0
                
                val bestSet = bestSets[setNumber]
                val bestWeight = bestSet?.get("weight") as? Double
                val bestReps = (bestSet?.get("reps") as? Long)?.toInt()
                
                val previousCompletedSet = CompletedSet(
                    setNumber = setNumber,
                    weight = prevWeight,
                    reps = prevReps
                )
                
                val bestCompletedSet = if (bestWeight != null && bestReps != null) {
                    CompletedSet(
                        setNumber = setNumber,
                        weight = bestWeight,
                        reps = bestReps
                    )
                } else null
                
                result[setNumber] = Pair(previousCompletedSet, bestCompletedSet)
                Log.d(TAG, "ADDED SET $setNumber TO RESULT: PREV=${prevWeight}x${prevReps}, BEST=${bestWeight}x${bestReps}")
            }
            
            // Fill in missing set numbers
            val maxSetNumber = (previousSets.maxOfOrNull { 
                (it["setNumber"] as? Long)?.toInt() ?: 0 
            } ?: 0).coerceAtLeast(bestSets.keys.maxOrNull() ?: 0)
            
            for (i in 1..maxSetNumber) {
                if (!result.containsKey(i)) {
                    // If we have a best but no previous
                    val bestSet = bestSets[i]
                    if (bestSet != null) {
                        val bestWeight = bestSet["weight"] as? Double ?: 0.0
                        val bestReps = (bestSet["reps"] as? Long)?.toInt() ?: 0
                        
                        val bestCompletedSet = CompletedSet(
                            setNumber = i,
                            weight = bestWeight,
                            reps = bestReps
                        )
                        
                        result[i] = Pair(null, bestCompletedSet)
                        Log.d(TAG, "ADDED BEST-ONLY SET $i: BEST=${bestWeight}x${bestReps}")
                    } else {
                        result[i] = Pair(null, null)
                        Log.d(TAG, "ADDED EMPTY SET $i")
                    }
                }
            }
            
            // Log final results for debugging
            Log.d(TAG, "FINAL RESULT: ${result.size} SETS")
            result.forEach { (setNum, pair) ->
                val (prev, best) = pair
                Log.d(TAG, "SET $setNum: PREV=${prev?.weight}x${prev?.reps}, BEST=${best?.weight}x${best?.reps}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERROR FINDING SETS: ${e.message}", e)
        }
        return result
    }

    fun initializeFromRoutine(routineId: String) {
        if (isInitialized) return

        viewModelScope.launch {
            try {
                // Fetch the routine
                val routine = workoutRepository.getWorkoutById(routineId) ?: return@launch
                currentRoutineId = routineId
                originalRoutineName = routine.name
                _routineName.value = routine.name
                
                // Build WorkoutExercise list with pre-filled previous and best values
                val workoutExercises = routine.exercises.map { exercise ->
                    // Get previous and best sets for this exercise
                    Log.d(TAG, "Initializing sets for exercise: ${exercise.name} (${exercise.id})")
                    val previousAndBestSets = findPreviousAndBestSets(exercise.id)
                    Log.d(TAG, "Found ${previousAndBestSets.size} previous/best set entries")
                    
                    WorkoutExercise(
                        exercise = exercise,
                        sets = List(exercise.defaultSets) { idx ->
                            val setNumber = idx + 1
                            val (previousSet, bestSet) = previousAndBestSets[setNumber] ?: Pair(null, null)
                            
                            Log.d(TAG, "Set $setNumber - prev: ${previousSet?.weight}kg x ${previousSet?.reps}, best: ${bestSet?.weight}kg x ${bestSet?.reps}")
                            
                            // DIRECTLY USE VALUES FROM HISTORY RATHER THAN NULL
                            val prevReps = previousSet?.reps 
                            val prevWeight = previousSet?.weight
                            val bestReps = bestSet?.reps
                            val bestWeight = bestSet?.weight
                            
                            Log.d(TAG, "CREATING SET $setNumber WITH VALUES - PREV: $prevWeight×$prevReps, BEST: $bestWeight×$bestReps")
                            
                            ExerciseSet(
                                setNumber = setNumber,
                                reps = exercise.defaultReps,
                                weight = if (exercise.usesWeight && !exercise.isBodyweight) 0.0 else 0.0,
                                isCompleted = false,
                                previousReps = prevReps,
                                previousWeight = prevWeight,
                                bestReps = bestReps,
                                bestWeight = bestWeight
                            )
                        }
                    )
                }

                _exercises.value = workoutExercises
                isInitialized = true
                calculateStats()

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing routine", e)
                _workoutState.value = ActiveWorkoutState.Error(e.message ?: "Failed to load routine")
            }
        }
    }


    fun addExercise(ex: Exercise) {
        val list = _exercises.value.toMutableList()
        if (list.any { it.exercise.id == ex.id }) return
        val newSets = List(ex.defaultSets) { idx ->
            ExerciseSet(
                setNumber = idx + 1,
                reps      = ex.defaultReps,
                weight    = if (ex.usesWeight && !ex.isBodyweight) 0.0 else 0.0,
                isCompleted = false,
                previousReps = null,
                previousWeight = null
            )
        }
        list += WorkoutExercise(ex, newSets)
        _exercises.value = list
        isRoutineModified = currentRoutineId != null       // flag for later update-dialog
    }

    fun addSet(we: WorkoutExercise) {
        val exercises = _exercises.value.toMutableList()
        val idx       = exercises.indexOf(we); if (idx == -1) return
        val sets      = exercises[idx].sets.toMutableList()
        val prevReps  = sets.lastOrNull()?.reps ?: we.exercise.defaultReps

        sets += ExerciseSet(
            setNumber      = sets.size + 1,
            reps           = prevReps,
            weight         = if (we.exercise.usesWeight && !we.exercise.isBodyweight) 0.0 else 0.0,
            previousReps   = prevReps,
            previousWeight = null,
            isCompleted    = false
        )
        exercises[idx] = exercises[idx].copy(sets = sets)
        _exercises.value = exercises
    }

    fun deleteSet(we: WorkoutExercise, setNum: Int) {
        val list = _exercises.value.toMutableList()
        val i    = list.indexOf(we); if (i == -1) return
        val newSets = list[i].sets
            .filterNot { it.setNumber == setNum }
            .mapIndexed { idx, st -> st.copy(setNumber = idx + 1) }
        list[i] = list[i].copy(sets = newSets)
        _exercises.value = list
        calculateStats()
    }

    fun updateWeight(we: WorkoutExercise, set: ExerciseSet, kg: Double) =
        mutateSet(we, set) { cur ->
            // Only update best if completing set and new value is better than previous best
            val bestWeight = if (cur.isCompleted) {
                maxOf(cur.bestWeight ?: 0.0, kg)
            } else {
                cur.bestWeight
            }
            cur.copy(weight = kg, bestWeight = bestWeight)
        }

    fun updateReps(we: WorkoutExercise, set: ExerciseSet, reps: Int) =
        mutateSet(we, set) { cur ->
            // Only update best if completing set and new value is better than previous best
            val bestReps = if (cur.isCompleted) {
                maxOf(cur.bestReps ?: 0, reps)
            } else {
                cur.bestReps
            }
            cur.copy(reps = reps, bestReps = bestReps)
        }

    fun setCompleted(we: WorkoutExercise, set: ExerciseSet, completed: Boolean) =
        mutateSet(we, set) { cur -> 
            // When marking as completed, update best values if this set is better
            val bestReps = if (completed) {
                maxOf(cur.bestReps ?: 0, cur.reps)  
            } else {
                cur.bestReps
            }
            
            val bestWeight = if (completed) {
                maxOf(cur.bestWeight ?: 0.0, cur.weight)
            } else {
                cur.bestWeight
            }
            
            cur.copy(
                isCompleted = completed,
                bestReps = bestReps,
                bestWeight = bestWeight
            ) 
        }

    fun updateNotes(we: WorkoutExercise, notes: String) {
        val list = _exercises.value.toMutableList()
        val i    = list.indexOf(we); if (i == -1) return
        list[i]  = list[i].copy(notes = notes)
        _exercises.value = list
    }

    /* -------------   FINISH WORKOUT   ------------- */
    fun finishWorkout(onSuccess: () -> Unit) = viewModelScope.launch {
        try {
            _workoutState.value = ActiveWorkoutState.Loading

            /* --------- gather data for history doc --------- */
            val uid   = authRepository.getCurrentUser()?.uid
                ?: throw IllegalStateException("User not authenticated")

            val routineColour = currentRoutineId
                ?.let { workoutRepository.getWorkoutById(it)?.colorHex }   // ← workoutRepo
                ?: "#6B9CD6"

            val nameForHistory = originalRoutineName ?: generateWorkoutName()

            val doneExercises = _exercises.value
                .filter { ex -> ex.sets.any(ExerciseSet::isCompleted) }

            if (doneExercises.isEmpty()) {
                throw IllegalStateException("No completed sets")
            }

            /* ---- build CompletedExercise + CompletedSet objects with best numbers ---- */
            val completedExercises = doneExercises.map { wEx ->
                // CRITICAL: Make sure to save exercise ID
                val exId = wEx.exercise.id
                
                Log.d(TAG, "SAVING EXERCISE: ${wEx.exercise.name} (ID: '$exId') with ${wEx.sets.count(ExerciseSet::isCompleted)} completed sets")
                
                CompletedExercise(
                    exerciseId = exId, // Make sure this is populated!
                    name       = wEx.exercise.name,
                    notes      = wEx.notes,
                    sets       = wEx.sets.filter(ExerciseSet::isCompleted).map { set ->
                        // Track the best performance including this set
                        val bestWeight = maxOf(set.weight, set.bestWeight ?: 0.0)
                        val bestReps = maxOf(set.reps, set.bestReps ?: 0)
                        
                        Log.d(TAG, "SAVING SET ${set.setNumber}: current=${set.weight}×${set.reps}, best=$bestWeight×$bestReps")
                        
                        CompletedSet(
                            setNumber = set.setNumber, 
                            weight = set.weight, 
                            reps = set.reps,
                            bestWeight = bestWeight,
                            bestReps = bestReps
                        )
                    }
                )
            }


            val history = WorkoutHistory(
                id           = UUID.randomUUID().toString(),
                name         = nameForHistory,
                userId       = uid,
                startTime    = startMs,
                endTime      = System.currentTimeMillis(),
                exercises    = completedExercises,
                totalVolume  = _totalVolume.value.toDouble(),
                totalSets    = _totalSets.value,
                colorHex     = routineColour,
                routineId    = currentRoutineId,
                exerciseIds  = completedExercises.map { it.exerciseId }
            )

            historyRepo.saveWorkoutHistory(history)

            /* ---- update routine meta & maybe ask to update routine defaults ---- */
            currentRoutineId?.let { rid ->
                workoutRepository.updateWorkoutLastPerformed(rid, history.endTime)

                if (isRoutineModified) {
                    _showUpdateRoutineDialog.value = true
                    _workoutState.value            = ActiveWorkoutState.Success
                    return@launch                          // UI will decide next step
                }
            }

            /* ---- normal successful finish ---- */
            isWorkoutActive      = false     // stop timer
            _workoutState.value  = ActiveWorkoutState.Success
            onSuccess()

        } catch (e: Exception) {
            Log.e(TAG, "finishWorkout error", e)
            _workoutState.value = ActiveWorkoutState.Error(e.message ?: "Error saving workout")
        }
    }

    /* -------------   ROUTINE-UPDATE CONFIRMATION   ------------- */
    fun updateRoutine(onSuccess: () -> Unit) = viewModelScope.launch {
        try {
            currentRoutineId?.let { rid ->
                val original = workoutRepository.getWorkoutById(rid) ?: return@launch
                val patched  = _exercises.value.map { it.exercise.copy(
                    defaultReps = it.sets.maxOf { s -> s.reps },
                    defaultSets = it.sets.size
                )}
                workoutRepository.updateWorkout(original.copy(
                    exercises     = patched,
                    lastPerformed = System.currentTimeMillis()
                ))
            }
            isRoutineModified    = false
            isWorkoutActive      = false
            _workoutState.value  = ActiveWorkoutState.Success
            onSuccess()
        } catch (e: Exception) {
            _workoutState.value = ActiveWorkoutState.Error(e.message ?: "Update failed")
        }
    }

    fun dismissUpdateDialog() { _showUpdateRoutineDialog.value = false }

    /* ─────────────────────────────────── internal helpers ─────────────────────────────────── */

    private fun mutateSet(
        we: WorkoutExercise,
        target: ExerciseSet,
        transform: (ExerciseSet) -> ExerciseSet
    ) {
        val list  = _exercises.value.toMutableList()
        val exIdx = list.indexOf(we); if (exIdx == -1) return
        val sets  = list[exIdx].sets.toMutableList()
        val stIdx = sets.indexOfFirst { it.setNumber == target.setNumber }; if (stIdx == -1) return
        sets[stIdx] = transform(sets[stIdx])
        list[exIdx] = list[exIdx].copy(sets = sets)
        _exercises.value = list
        calculateStats()
    }

    private fun calculateStats() {
        var vol = 0
        var cnt = 0
        _exercises.value.forEach { we ->
            we.sets.filter(ExerciseSet::isCompleted).forEach {
                vol += (it.weight * it.reps).toInt()
                cnt += 1
            }
        }
        _totalVolume.value = vol
        _totalSets.value   = cnt
    }

    private fun startTimer() = viewModelScope.launch {
        while (isWorkoutActive) {
            val s = ((System.currentTimeMillis() - startMs) / 1000).toInt()
            _workoutDuration.value = when {
                s >= 3600 -> "%dh %dm %ds".format(s / 3600, (s % 3600) / 60, s % 60)
                s >=   60 -> "%dm %ds".format(s / 60, s % 60)
                else      -> "${s}s"
            }
            delay(1_000)
        }
    }

    private fun generateWorkoutName(): String {
        val groups = _exercises.value.map { it.exercise.muscleGroup }.distinct()
        return when (groups.size) {
            0  -> "Quick Workout"
            1  -> "${groups[0]} Workout"
            else -> "${groups[0]} & ${groups[1]} Workout"
        }
    }

    fun discardWorkout() { isWorkoutActive = false }

    override fun onCleared() {
        super.onCleared()
        isWorkoutActive = false
    }
}