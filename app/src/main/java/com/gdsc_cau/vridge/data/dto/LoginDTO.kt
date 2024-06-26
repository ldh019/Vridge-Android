package com.gdsc_cau.vridge.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginDTO(
    val token: String,
    @SerialName("fcm_token") val fcmToken: String
)
