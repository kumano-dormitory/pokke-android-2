package com.kumanodormitory.pokke.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncPushRequest(
    val deviceId: String,
    val generatedAt: Long,
    val parcels: SyncPushParcelRequest
)

@Serializable
data class SyncPushParcelRequest(
    val mode: String = "DIFF",
    val items: List<ParcelDto>
)
