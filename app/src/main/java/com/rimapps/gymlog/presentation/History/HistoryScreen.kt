package com.rimapps.gymlog.presentation.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rimapps.gymlog.R
import com.rimapps.gymlog.domain.model.MonthlyCalendarDay
import com.rimapps.gymlog.domain.model.RoutineSummary
import com.rimapps.gymlog.ui.theme.vag
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBack  : () -> Unit
) {
    val month         = viewModel.currentMonth.collectAsState().value
    val calendarDays  = viewModel.calendarDays .collectAsState().value
    val routines      = viewModel.routines     .collectAsState().value

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black)
                }
                Text("History",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = vag,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        /* ─────────── Month navigator ─────────── */
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            ArrowButton(R.drawable.leftarrow, viewModel::pagePrev)
            Text(
                month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " ${month.year}",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            ArrowButton(R.drawable.rightarrow, viewModel::pageNext)
        }

        Spacer(Modifier.height(24.dp))

        MonthlyCalendar(calendarDays)

        Spacer(Modifier.height(24.dp))

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("Routines",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                contentPadding      = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(routines) { summary -> RoutineCard(summary) }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RoutineCard(r: RoutineSummary) {

    val ring   = Color(android.graphics.Color.parseColor(r.colorHex))
    val fill   = ring.copy(alpha = .35f)
    val now    = System.currentTimeMillis()
    val days   = Duration.ofMillis(now - r.lastPerformed).toDays()

    Row(
        Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(r.name,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text("${r.timesDone} × this month",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = vag,
                color = Color.Gray
            )
            Text(
                if (days == 0L) "Today" else "$days days ago",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = vag,
                color = Color.Gray
            )
        }

        Box(
            Modifier
                .size(14.dp)
                .background(fill, CircleShape)
                .border(1.dp, ring, CircleShape)
        )
    }
}


@Composable
private fun ArrowButton(iconRes: Int, onClick: () -> Unit) {
    IconButton(
        onClick   = onClick,
        modifier  = Modifier
            .size(36.dp)
            .border(1.dp, Color.Black, CircleShape)
    ) {
        Icon(
            painterResource(id = iconRes),
            contentDescription = null,
            tint   = Color.Black,
            modifier = Modifier.size(20.dp)
        )
    }
}

/*  … imports & other composables unchanged …  */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthlyCalendar(days: List<MonthlyCalendarDay>) {

    val cell = 40.dp
    val cols = 7
    val rows = (days.size + cols - 1) / cols

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        repeat(rows) { r ->
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {

                repeat(cols) { c ->
                    val i = r * cols + c
                    if (i < days.size) {
                        val day = days[i]

                        val ring  = day.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
                            ?: Color.Gray
                        val fill  = day.colorHex?.let { ring.copy(alpha = .35f) }
                            ?: Color.Transparent
                        val textC = if (day.colorHex != null) Color.Black else Color.Black

                        Box(
                            Modifier.size(cell)
                                .background(fill, CircleShape)
                                .border(1.dp, ring, CircleShape),
                            Alignment.Center
                        ) {
                            Text(
                                "${day.date.dayOfMonth}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = vag,
                                color = textC,
                                fontWeight = if (day.colorHex != null) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    } else Spacer(Modifier.size(cell))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
