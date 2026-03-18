package com.kumanodormitory.pokke.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RyoseiListResponse(val ryosei: List<RyoseiDto>)

@Serializable
data class RyoseiDto(
    val id: String,
    val name: String,
    val nameKana: String,
    val nameAlphabet: String,
    val room: String,
    val block: String,
    val leavingDate: Long? = null,
    val discordStatus: String = "UNLINKED"
)
