package com.abdallah.powertrack.models

import com.google.gson.annotations.SerializedName

data class BuyTokenRequest(
    @SerializedName("amount") val amount: Float,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("meter_number") val meterNumber: String,
    @SerializedName("payment_method") val paymentMethod: String
)

data class BuyTokenResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String?,
    @SerializedName("units") val units: Float?,
    @SerializedName("amount") val amount: Float?,
    @SerializedName("checkout_request_id") val checkoutRequestId: String?
)

data class TransactionHistoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("amount") val amount: Float,
    @SerializedName("units") val units: Float,
    @SerializedName("token") val token: String,
    @SerializedName("status") val status: String,
    @SerializedName("date") val date: String
)

data class BalanceResponse(
    @SerializedName("meter_number") val meterNumber: String,
    @SerializedName("balance") val balance: Float
)
