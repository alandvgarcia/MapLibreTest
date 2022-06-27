package com.alandvgarcia.maplibretest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alandvgarcia.maplibretest.ui.theme.MapLibreTestTheme
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        Mapbox.getInstance(this)

        super.onCreate(savedInstanceState)
        setContent {
            MapLibreTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapView()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapView() {

    val mapView = rememberMapViewWithLifecycle()


   Scaffold(topBar = {  CenterAlignedTopAppBar(title = { Text("Map") }) }) {
       Column(modifier = Modifier.padding(it)) {
            AndroidView(factory = {
                mapView
            })
       }
   }
}


@Composable
private fun rememberMapViewWithLifecycle(
    position: LatLng? = null,
    zoomLevel: Double = 15.0,
): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            getMapAsync { mapboxMap ->

                Log.d("Styles ->", "${Style.getPredefinedStyles().map { it.url }}")

                mapboxMap.setStyle("https://api.maptiler.com/maps/hybrid/style.json?key=xb584Ck62OfuAx80HYuS")
                if (position != null) {
                    val cameraPosition = CameraPosition.Builder()
                        .target(position)
                        .zoom(zoomLevel)
                        .build()
                    mapboxMap.cameraPosition = cameraPosition
                }
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return mapView
}



private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> mapView.onDestroy()
        }
    }

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MapLibreTestTheme {
        MapView()
    }
}