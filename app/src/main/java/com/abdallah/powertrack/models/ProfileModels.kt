package com.abdallah.powertrack.models

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("meter_number") val meterNumber: String,
    @SerializedName("email") val email: String? = null
)

data class ProfileResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("profile") val profile: UserProfile?
)
