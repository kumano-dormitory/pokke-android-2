package com.kumanodormitory.pokke.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ryosei")
data class RyoseiEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "name_kana")
    val nameKana: String,
    @ColumnInfo(name = "name_alphabet")
    val nameAlphabet: String,
    val room: String,
    val block: String,
    @ColumnInfo(name = "leaving_date")
    val leavingDate: Long? = null,
    @ColumnInfo(name = "discord_status")
    val discordStatus: String = DiscordStatus.UNLINKED.name
)
