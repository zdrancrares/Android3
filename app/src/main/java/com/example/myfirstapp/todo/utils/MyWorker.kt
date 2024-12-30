package com.example.myfirstapp.todo.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myfirstapp.MyFirstApplication

class MyWorker(
    context: Context,
    val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val itemRepository = (applicationContext as MyFirstApplication).container.itemRepository

        val notSaved = itemRepository.getLocallySaved()
        Log.d("MyWorker", notSaved.toString())

        notSaved.forEach{ trip ->
            if(trip._id.length < 12){
                trip._id = ""
                val res = itemRepository.save(trip)  // am pus isSaved = true in repository
            }
            else{
                var res = itemRepository.update(trip)
            }
        }
        // perform long running operation
//        var s = 0
//        for (i in 1..workerParams.inputData.getInt("to", 1)) {
//            if (isStopped) {
//                break
//            }
//            SECONDS.sleep(1)
//            Log.d("MyWorker", "progress: $i")
//            setProgressAsync(workDataOf("progress" to i))
//            s += i
//        }
        return Result.success()
    }
}