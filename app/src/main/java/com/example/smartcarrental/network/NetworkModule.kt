package com.example.smartcarrental.network


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val openAIService: OpenAIService = retrofit.create(OpenAIService::class.java)

    private val mapsRetrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val directionsApi: DirectionsApi =
        mapsRetrofit.create(DirectionsApi::class.java)

    val placesRetrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val placesApi = placesRetrofit.create(PlacesApi::class.java)
}
