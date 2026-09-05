package com.example.data.remote

import com.example.data.model.Transaction
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class CloudTransactionsData(
    val transactions: List<Transaction> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class CloudObjectResponse(
    val id: String? = null,
    val name: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val data: CloudTransactionsData? = null
)

@JsonClass(generateAdapter = true)
data class CloudObjectRequest(
    val name: String = "CashTrack_Shared_Main_Boss",
    val data: CloudTransactionsData
)

interface CloudSyncApi {
    @GET("objects/{id}")
    suspend fun getSharedData(@Path("id") id: String): Response<CloudObjectResponse>

    @PUT("objects/{id}")
    suspend fun updateSharedData(
        @Path("id") id: String,
        @Body request: CloudObjectRequest
    ): Response<CloudObjectResponse>

    @POST("objects")
    suspend fun createSharedData(
        @Body request: CloudObjectRequest
    ): Response<CloudObjectResponse>

    companion object {
        const val BASE_URL = "https://api.restful-api.dev/"
        // The permanently provisioned shared cloud database ID for Cash Track
        const val DEFAULT_SHARED_DATABASE_ID = "ff808181a067127101a07093793316d3"

        fun create(): CloudSyncApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(CloudSyncApi::class.java)
        }
    }
}
