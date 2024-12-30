package com.example.myfirstapp.todo.data.remote

import com.example.myfirstapp.todo.data.Trip

data class Payload(val updatedTrip: Trip)

data class ItemEvent(val event: String, val payload: Payload)
