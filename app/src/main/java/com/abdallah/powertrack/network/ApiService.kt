package com.abdallah.powertrack.network

import com.abdallah.powertrack.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("buy-token/")
    suspend fun buyToken(@Body request: BuyTokenRequest): Response<BuyTokenResponse>

    @GET("check-status/{checkout_request_id}/")
    suspend fun checkStatus(@Path("checkout_request_id") checkoutRequestId: String): Response<BuyTokenResponse>

    @GET("history/{meter_number}/")
    suspend fun getHistory(@Path("meter_number") meterNumber: String): Response<List<TransactionHistoryItem>>

    @GET("balance/{meter_number}/")
    suspend fun getBalance(@Path("meter_number") meterNumber: String): Response<BalanceResponse>

    @GET("profile/")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("profile/update/")
    suspend fun updateProfile(@Body profile: UserProfile): Response<ProfileResponse>
}
