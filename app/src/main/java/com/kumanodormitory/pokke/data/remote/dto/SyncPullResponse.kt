package com.kumanodormitory.pokke.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncPullResponse(
    val serverTime: Long,
    val parcels: SyncPullParcelResponse,
    val ryosei: SyncPullRyoseiResponse
)

@Serializable
data class SyncPullParcelResponse(
    val mode: String,
    val items: List<ParcelDto> = emptyList(),
    val upserted: List<ParcelDto> = emptyList()
)

@Serializable
data class SyncPullRyoseiResponse(
    val mode: String,
    val version: Long = 0,
    val items: List<RyoseiDto> = emptyList()
)
