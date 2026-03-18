package com.kumanodormitory.pokke.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncPullRequest(
    val deviceId: String,
    val parcels: SyncPullParcelRequest,
    val ryosei: SyncPullRyoseiRequest
)

@Serializable
data class SyncPullParcelRequest(
    val mode: String,
    val since: Long? = null
)

@Serializable
data class SyncPullRyoseiRequest(
    val mode: String = "SNAPSHOT"
)
