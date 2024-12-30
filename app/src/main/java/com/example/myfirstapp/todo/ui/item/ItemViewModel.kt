package com.example.myfirstapp.todo.ui.item

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.myfirstapp.MyFirstApplication
import com.example.myfirstapp.core.Result
import com.example.myfirstapp.core.TAG
import com.example.myfirstapp.todo.data.Trip
import com.example.myfirstapp.todo.data.ItemRepository
import com.example.myfirstapp.todo.utils.MyNetworkStatusViewModel
import com.example.myfirstapp.todo.utils.MyWorker
import kotlinx.coroutines.launch
import java.util.UUID

data class ItemUiState(
    val itemId: String? = null,
    val trip: Trip = Trip(),
    var loadResult: Result<Trip>? = null,
    var submitResult: Result<Trip>? = null,
)

class ItemViewModel(private val itemId: String?, private val itemRepository: ItemRepository) :
    ViewModel() {

    var uiState: ItemUiState by mutableStateOf(ItemUiState(loadResult = Result.Loading))
        private set

    init {
        Log.d(TAG, "init with id: ${itemId}")
        if (itemId != null) {
            loadItem()
        } else {
            uiState = uiState.copy(loadResult = Result.Success(Trip()))
        }
    }

    fun loadItem() {
        viewModelScope.launch {
            itemRepository.tripStream.collect { items ->
                if (!(uiState.loadResult is Result.Loading)) {
                    return@collect
                }
                Log.d(TAG, "searching for ${itemId}")
                val trip = items.find { it._id == itemId } ?: Trip()
                Log.d(TAG, "found ${trip}")
                uiState = uiState.copy(trip = trip, loadResult = Result.Success(trip))
            }
        }
    }

    fun saveItem(destination: String, budget: Double, dateOfTrip: String, withCar: Boolean){
        viewModelScope.launch {
            Log.d(TAG, "save new trip!!!");
            try{
                uiState = uiState.copy(submitResult = Result.Loading)
                val item = uiState.trip.copy(destination=destination, budget = budget, dateOfTrip = dateOfTrip, withCar = withCar, _id = "")
                val savedTrip: Trip;
                savedTrip = itemRepository.save(item)
                Log.d(TAG, "save trip succeeeded!!!!");
                uiState = uiState.copy(submitResult = Result.Success(savedTrip))
            }catch (e: Exception){
                Log.d(TAG, "saveOrUpdateItem failed");
                val nrUnsaved = itemRepository.getNrUnsaved()
                val currentId = nrUnsaved + 1
                uiState = uiState.copy(submitResult = Result.Error(e))
                val item = uiState.trip.copy(destination=destination, budget = budget, dateOfTrip = dateOfTrip, withCar = withCar, isSaved = false, _id = currentId.toString())
                itemRepository.addLocally(item);
                Log.d(TAG, "added item ${item} locally");
            }
        }
    }

    fun UpdateItem(destination: String, budget: Double, lat: Double, lon: Double) {
        viewModelScope.launch {
            Log.d(TAG, "update trip!!!");
            try {
                uiState = uiState.copy(submitResult = Result.Loading)
                val item = uiState.trip.copy(destination=destination, budget = budget, isSaved = true, lat = lat, lon = lon)
                val savedTrip: Trip;
                savedTrip = itemRepository.update(item)
                Log.d(TAG, "UpdateItem succeeeded");
                uiState = uiState.copy(submitResult = Result.Success(savedTrip))
            } catch (e: Exception) {
                Log.d(TAG, "saveOrUpdateItem failed");
                uiState = uiState.copy(submitResult = Result.Error(e))
                val item = uiState.trip.copy(destination=destination, budget = budget, isSaved = false, lat=lat, lon=lon)
                itemRepository.updateLocally(item)
            }
        }
    }

    companion object {
        fun Factory(itemId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyFirstApplication)
                ItemViewModel(itemId, app.container.itemRepository)
            }
        }
    }
}
