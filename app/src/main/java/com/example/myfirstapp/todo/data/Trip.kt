package com.example.myfirstapp.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(@PrimaryKey var _id: String = "",
                val destination: String = "",
                val budget: Double=0.0,
                val dateOfTrip: String="",
                val withCar: Boolean= false,
                var isSaved: Boolean=true,
                var lat: Double=0.0,
                var lon: Double=0.0)
