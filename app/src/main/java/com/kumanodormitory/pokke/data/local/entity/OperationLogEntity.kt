package com.kumanodormitory.pokke.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operation_logs")
data class OperationLogEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "parcel_id")
    val parcelId: String? = null,
    @ColumnInfo(name = "operation_type")
    val operationType: String,
    @ColumnInfo(name = "operated_by_name")
    val operatedByName: String? = null,
    val metadata: String? = null
)
