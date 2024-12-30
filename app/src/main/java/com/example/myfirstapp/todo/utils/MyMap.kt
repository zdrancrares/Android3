package com.example.myfirstapp.todo.utils

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

val TAG = "MyMap"

@Composable
fun MyMap(lat: Double, lon: Double, onLocationChanged: (Double, Double) -> Unit) {
    Log.d("MyMap!!!!", "Location: ${lat}, ${lon}")

    val markerState = remember { MarkerState(position = LatLng(lat, lon)) }

    LaunchedEffect(lat, lon) {
        markerState.position = LatLng(lat, lon)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerState.position, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = {
            Log.d(TAG, "onMapClick $it")
        },
        onMapLongClick = {
            Log.d(TAG, "onMapLongClick $it")
            // Update marker position when the map is long-clicked
            markerState.position = it
            onLocationChanged(it.latitude, it.longitude)
        },
    ) {
        Marker(
            state = markerState, // Use the remembered marker state
            title = "User location title",
            snippet = "User location",
        )
    }
}

