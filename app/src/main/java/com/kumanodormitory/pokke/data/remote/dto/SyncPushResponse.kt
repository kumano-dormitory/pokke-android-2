package com.kumanodormitory.pokke.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncPushResponse(
    val serverTime: Long,
    val accepted: SyncPushAccepted
)

@Serializable
data class SyncPushAccepted(
    val parcels: Int = 0
)
