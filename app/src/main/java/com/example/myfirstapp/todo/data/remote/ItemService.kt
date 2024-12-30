package com.example.myfirstapp.todo.data.remote

import com.example.myfirstapp.todo.data.Trip
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ItemService {
    @GET("/api/trip")
    suspend fun find(@Header("Authorization") authorization: String): List<Trip>

    @GET("/api/trip/{id}")
    suspend fun read( @Header("Authorization") authorization: String, @Path("id") itemId: String?): Trip;

    @Headers("Content-Type: application/json")
    @POST("/api/trip")
    suspend fun create(@Header("Authorization") authorization: String, @Body trip: Trip): Trip

    @Headers("Content-Type: application/json")
    @PUT("/api/trip/{id}")
    suspend fun update( @Header("Authorization") authorization: String, @Path("id") itemId: String?, @Body trip: Trip): Trip
}
