package com.kumanodormitory.pokke.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ParcelSyncResponse(
    val syncedIds: List<String> = emptyList()
)
