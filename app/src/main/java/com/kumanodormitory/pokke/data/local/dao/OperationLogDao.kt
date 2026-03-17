package com.kumanodormitory.pokke.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kumanodormitory.pokke.data.local.entity.OperationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationLogDao {

    @Query("SELECT * FROM operation_logs ORDER BY created_at DESC LIMIT 50")
    fun getRecent(): Flow<List<OperationLogEntity>>

    @Query(
        """
        SELECT * FROM operation_logs
        WHERE created_at BETWEEN :fromMillis AND :toMillis
        ORDER BY created_at DESC
        """
    )
    fun getByDateRange(fromMillis: Long, toMillis: Long): Flow<List<OperationLogEntity>>

    @Query(
        """
        SELECT ol.* FROM operation_logs ol
        INNER JOIN parcels p ON ol.parcel_id = p.id
        WHERE ol.created_at BETWEEN :fromMillis AND :toMillis
          AND p.owner_block LIKE :building || '%'
        ORDER BY ol.created_at DESC
        """
    )
    fun getByDateRangeAndBuilding(fromMillis: Long, toMillis: Long, building: String): Flow<List<OperationLogEntity>>

    @Query("SELECT * FROM operation_logs WHERE id = :id")
    suspend fun getById(id: String): OperationLogEntity?

    @Insert
    suspend fun insert(log: OperationLogEntity)
}
