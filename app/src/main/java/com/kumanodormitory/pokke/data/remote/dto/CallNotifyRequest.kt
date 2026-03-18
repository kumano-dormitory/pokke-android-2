package com.kumanodormitory.pokke.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CallNotifyRequest(
    val ryoseiId: String,
    val reason: String,
    val message: String? = null
)
