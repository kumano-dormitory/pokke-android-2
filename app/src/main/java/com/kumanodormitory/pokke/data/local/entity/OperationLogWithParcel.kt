package com.kumanodormitory.pokke.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class OperationLogWithParcel(
    @Embedded val log: OperationLogEntity,
    @ColumnInfo(name = "parcel_owner_room") val parcelOwnerRoom: String?,
    @ColumnInfo(name = "parcel_owner_name") val parcelOwnerName: String?
)
