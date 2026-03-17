package com.kumanodormitory.pokke.data.local.entity

enum class DiscordStatus {
    LINKED,
    UNLINKED
}

enum class ParcelType {
    NORMAL,
    REFRIGERATED,
    FROZEN,
    LARGE,
    ABSENCE_SLIP,
    OTHER
}

enum class ParcelStatus {
    REGISTERED,
    RECEIVED
}

enum class ParcelOperationType {
    REGISTER,
    DELIVER,
    CANCEL_REGISTER,
    CANCEL_DELIVER,
    MARK_LOST,
    NIGHT_DUTY_CONFIRM,
    DUTY_CHANGE
}
