package com.example.myfirstapp.todo.data

import android.util.Log
import com.example.myfirstapp.core.Result
import com.example.myfirstapp.core.TAG
import com.example.myfirstapp.core.data.remote.Api
import com.example.myfirstapp.todo.data.local.ItemDao
import com.example.myfirstapp.todo.data.remote.ItemEvent
import com.example.myfirstapp.todo.data.remote.ItemService
import com.example.myfirstapp.todo.data.remote.ItemWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class ItemRepository(private val itemService: ItemService,
                     private val itemWsClient: ItemWsClient,
                     private val itemDao: ItemDao) {

    val tripStream by lazy { itemDao.getAll() }

    init {
        Log.d(TAG, "init")
    }

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            val trips = itemService.find(authorization = getBearerToken())
            itemDao.deleteAll()
            trips.forEach{ itemDao.insert(it) }
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getItemEvents().collect {
                Log.d(TAG, "Item event collected $it")
                if (it is Result.Success) {
                    val itemEvent = it.data;
                    when (itemEvent.event) {
                        "created" -> handleItemCreated(itemEvent.payload.updatedTrip)
                        "updated" -> handleItemUpdated(itemEvent.payload.updatedTrip)
                        "deleted" -> handleItemDeleted(itemEvent.payload.updatedTrip)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            itemWsClient.closeSocket()
        }
    }

    suspend fun getItemEvents(): Flow<Result<ItemEvent>> = callbackFlow {
        Log.d(TAG, "getItemEvents started")
        itemWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) {
                    Log.d(TAG, "onEvent trySend $it")
                    trySend(Result.Success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() });
        awaitClose { itemWsClient.closeSocket() }
    }

    suspend fun update(trip: Trip): Trip {
        Log.d(TAG, "update $trip...")
        trip.isSaved = true
        val updatedItem = itemService.update(authorization = getBearerToken(), trip._id, trip)
        Log.d(TAG, "update $trip succeeded")
        handleItemUpdated(updatedItem)
        return updatedItem
    }

    suspend fun save(trip: Trip): Trip {
        Log.d(TAG, "save $trip...")
        trip.isSaved = true
        val createdItem = itemService.create(authorization = getBearerToken(), trip)
        Log.d(TAG, "save $trip succeeded")
        Log.d(TAG, "handle created $createdItem")
        handleItemCreated(createdItem)
        deleteTrip(createdItem.destination, false)
        return createdItem
    }

    suspend fun addLocally(trip: Trip){
        itemDao.insert(trip);
    }

    suspend fun deleteLocally(){
        itemDao.getLocalTrips(isSaved = false)
    }

    suspend fun getLocallySaved(): List<Trip>{
        return itemDao.getLocalTrips(isSaved = false)
    }

    suspend fun updateLocally(trip: Trip){
        Log.d(TAG, "Updating trip locally: ${trip}")
        itemDao.update(trip)
    }

    private suspend fun handleItemDeleted(trip: Trip) {
        Log.d(TAG, "handleItemDeleted - todo $trip")
    }

    private suspend fun handleItemUpdated(trip: Trip) {
        Log.d(TAG, "handleItemUpdated...: $trip")
        itemDao.update(trip)
    }

    private suspend fun handleItemCreated(trip: Trip) {
        Log.d(TAG, "handleItemCreated...: $trip")
        itemDao.insert(trip)
    }

    suspend fun deleteAll(){
        itemDao.deleteAll()
    }

    suspend fun deleteTrip(destination: String, isSaved: Boolean) {
        Log.d(TAG, "deleting not saved: ${destination}, ${isSaved}")
        itemDao.deleteTripNotSaved(destination, isSaved)
    }

    suspend fun getNrUnsaved(): Int{
        return itemDao.getNotSaved(false)
    }

    fun setToken(token: String) {
        itemWsClient.authorize(token)
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"
}