package com.rimapps.gymlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_colors")
data class RoutineColorEntity(
    @PrimaryKey val id: String,
    val colorHex: String,
    val name: String
)