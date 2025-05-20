package com.rimapps.gymlog.presentation.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.domain.model.MonthlyCalendarDay
import com.rimapps.gymlog.domain.model.RoutineSummary
import com.rimapps.gymlog.domain.model.WorkoutHistory
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.domain.repository.WorkoutHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepo : WorkoutHistoryRepository,
    private val authRepo    : AuthRepository
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val _uid = MutableStateFlow<String?>(null)

    init {                                 // fetch UID once
        viewModelScope.launch { _uid.value = authRepo.getCurrentUser()?.uid }
    }

    private val _month = MutableStateFlow( LocalDate.now(zone).withDayOfMonth(1) )
    val  currentMonth : StateFlow<LocalDate> = _month.asStateFlow()
    fun pageNext() = _month.update { it.plusMonths(1) }
    fun pagePrev() = _month.update { it.minusMonths(1) }



    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthWorkouts : Flow<List<WorkoutHistory>> =
        _uid.filterNotNull().flatMapLatest { uid ->
            _month.flatMapLatest { start ->
                historyRepo.getMonthlyHistory(uid, start, zone)
            }
        }

    val calendarDays : StateFlow<List<MonthlyCalendarDay>> =
        monthWorkouts.map { list ->
            val byDate = list.groupBy {
                Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
            }

            val start = _month.value
            (1..start.lengthOfMonth()).map { d ->
                val date = start.withDayOfMonth(d)
                val colour = byDate[date]?.firstOrNull()?.colorHex   // pick any (all same routine colour)
                MonthlyCalendarDay(date, colour)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val routines : StateFlow<List<RoutineSummary>> =
        monthWorkouts.map(::toRoutineSummaries)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun toRoutineSummaries(histories: List<WorkoutHistory>): List<RoutineSummary> =
        histories
            .filter   { it.routineId != null }                // ← DROP quick-workouts
            .groupBy  { it.routineId }                        // one bucket per saved routine
            .map      { (_, list) ->
                val latest = list.maxByOrNull { it.endTime }!!
                RoutineSummary(
                    routineId     = latest.routineId,         // never null now
                    name          = latest.name,              // already the routine’s name
                    colorHex      = latest.colorHex,
                    timesDone     = list.size,
                    lastPerformed = latest.endTime
                )
            }
            .sortedByDescending { it.lastPerformed }
}
