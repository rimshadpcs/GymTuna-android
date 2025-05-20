package com.rimapps.gymlog.presentation.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rimapps.gymlog.domain.model.WeeklyCalendarDay
import com.rimapps.gymlog.ui.theme.vag
import java.time.LocalDate
import androidx.core.graphics.toColorInt
import com.rimapps.gymlog.R

// WeeklyCalendarStrip.kt
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyCalendarStrip(
    weeklyCalendar: List<WeeklyCalendarDay>,
    modifier: Modifier = Modifier,
    onHistoryClick : () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = vag,
                fontWeight = FontWeight.SemiBold
            )

            // History outlined button
            OutlinedButton(
                onClick = onHistoryClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "History",
                        fontFamily = vag,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Image(
                        painter = painterResource(id = R.drawable.history),
                        contentDescription = "History",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyCalendar.forEach { day ->
                DayColumn(
                    day = day,
                    isToday = day.date == LocalDate.now(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayColumn(
    day: WeeklyCalendarDay,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val ringColour = day.colorHex
        ?.let { Color(android.graphics.Color.parseColor(it)) }
        ?: Color.Gray                      // placeholder while loading

    val fillColour = when {
        day.colorHex != null -> ringColour.copy(alpha = 0.35f)   // soft fill
        isToday             -> Color.Gray.copy(alpha = 0.10f)    // today highlight
        else                -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text  = day.date.dayOfWeek.name.take(3),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(fillColour, CircleShape)
                .border(                        // ← always draw ring for completed day
                    width = if (day.isCompleted) 1.dp else 0.dp,
                    color = ringColour,
                    shape = CircleShape
                )
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isToday || day.colorHex != null) Color.Black else Color.Gray,
                fontWeight = when {
                    isToday          -> FontWeight.Bold
                    day.colorHex != null -> FontWeight.SemiBold
                    else              -> FontWeight.Normal
                }
            )
        }
    }
}
