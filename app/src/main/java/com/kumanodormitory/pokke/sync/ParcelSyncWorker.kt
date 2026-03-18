package com.kumanodormitory.pokke.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kumanodormitory.pokke.data.local.PokkeDatabase
import com.kumanodormitory.pokke.data.remote.PokkeApiClient
import com.kumanodormitory.pokke.data.remote.dto.SyncPushRequest
import com.kumanodormitory.pokke.data.remote.dto.SyncPushParcelRequest
import java.io.IOException

class ParcelSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = PokkeDatabase.getInstance(applicationContext)
            val parcelDao = db.parcelDao()

            val tenMinutesAgo = System.currentTimeMillis() - 10 * 60 * 1000
            val unsyncedParcels = parcelDao.getUnsyncedParcels(tenMinutesAgo)

            if (unsyncedParcels.isEmpty()) {
                Log.d(TAG, "No unsynced parcels found")
                return Result.success()
            }

            Log.d(TAG, "Syncing ${unsyncedParcels.size} parcels")

            val prefs = applicationContext.getSharedPreferences("pokke_sync", Context.MODE_PRIVATE)
            val deviceId = prefs.getString("deviceId", null) ?: run {
                val id = "pokke-${android.os.Build.MODEL}-${System.currentTimeMillis()}"
                prefs.edit().putString("deviceId", id).apply()
                id
            }
            val dtos = unsyncedParcels.map { it.toSyncDto() }
            val request = SyncPushRequest(
                deviceId = deviceId,
                generatedAt = System.currentTimeMillis(),
                parcels = SyncPushParcelRequest(items = dtos)
            )
            val response = PokkeApiClient.service.syncPush(body = request)

            if (response.isSuccessful) {
                val acceptedCount = response.body()?.accepted?.parcels ?: unsyncedParcels.size
                parcelDao.updateSyncedAt(unsyncedParcels.map { it.id }, System.currentTimeMillis())
                Log.d(TAG, "Successfully synced $acceptedCount parcels")
                Result.success()
            } else {
                Log.w(TAG, "Sync failed with code ${response.code()}")
                Result.retry()
            }
        } catch (e: IOException) {
            Log.w(TAG, "Sync failed due to network error", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed with unexpected error", e)
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "parcel_sync_worker"
        private const val TAG = "ParcelSyncWorker"
    }
}
