package com.kumanodormitory.pokke.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CallNotifyResponse(
    val callId: String,
    val ryoseiId: String,
    val status: String,
    val createdAt: Long
)
