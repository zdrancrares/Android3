package com.example.myfirstapp.todo.utils

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import java.util.UUID
import android.util.Log
import androidx.compose.material3.Button
import java.util.concurrent.TimeUnit

class MyNetworkStatusViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf(false)
        private set

    private var workManager: WorkManager
    private var workId: UUID? = null

    init {
        collectNetworkStatus()
         workManager = WorkManager.getInstance(getApplication())
        //doPeriodicWork()
    }

    private fun collectNetworkStatus() {
        viewModelScope.launch {
            ConnectivityManagerNetworkMonitor(getApplication()).isOnline.collect {
                uiState = it;
            }
        }
    }

    fun startJob() {
        viewModelScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val inputData = Data.Builder()
                .build()
            val myWork = OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            workId = myWork.id
            //uiState = uiState.copy(isRunning = true)
            workManager.apply {
                // enqueue Work
                enqueue(myWork)
            }
        }
    }

    fun doPeriodicWork(){
        viewModelScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val inputData = Data.Builder()
                .build()
            val myWork = PeriodicWorkRequestBuilder<MyWorker>(10, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            workId = myWork.id
            //uiState = uiState.copy(isRunning = true)
            workManager.apply {
                // enqueue Work
                enqueue(myWork)
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MyNetworkStatusViewModel(application)
            }
        }
    }
}

@Composable
fun MyNetworkStatus() {
    val myNewtworkStatusViewModel = viewModel<MyNetworkStatusViewModel>(
        factory = MyNetworkStatusViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )

    Log.d("My network status", "recompose: ${myNewtworkStatusViewModel.uiState}")

    Text(
        "Is online: ${myNewtworkStatusViewModel.uiState}",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(16.dp) // Add your desired margin values here
    )

//    Button(onClick = {
//        Log.d("My network status", "Doing work...")
//        myNewtworkStatusViewModel.startJob()
//    }) {
//        Text("Do work")
//    }

    LaunchedEffect(myNewtworkStatusViewModel.uiState){
        if(myNewtworkStatusViewModel.uiState){
            myNewtworkStatusViewModel.startJob()
        }
    }
}