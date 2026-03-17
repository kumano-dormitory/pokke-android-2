package com.kumanodormitory.pokke.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duty_person")
data class DutyPersonEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
