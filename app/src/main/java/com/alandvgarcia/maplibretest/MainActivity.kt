package com.alandvgarcia.maplibretest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.Property
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alandvgarcia.maplibretest.ui.theme.MapLibreTestTheme
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_TOP
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

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


    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Map") }) }) {
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

                mapboxMap.getStyle {
                    val symbolManager = createSymbolManager(this, mapboxMap, it )
                    configureCottonAnnotations(listOf(
                        LatLng(-21.220721, -50.410465),
                        LatLng(-21.221183, -50.410885),

                        ), symbolManager)
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


fun createSymbolManager(
    mapView: MapView,
    mapboxMap: MapboxMap,
    style: Style,
): SymbolManager {
//    if (style.getLayer(COTTON_LAYER) == null && style.getSource(COTTON_SOURCE) == null) {
//
//    }
    createCottonSymbolLayer(style, mapView.context)
    return SymbolManager(mapView, mapboxMap, style)
}

fun configureCottonAnnotations(
    listCottons: List<LatLng>,
    symbolManager: SymbolManager
)  {
    try {
        val cottonsSymbols = listOf<LatLng>()
        listCottons
            .forEach { cottonItem ->
                val symbol = symbolManager.create(
                    SymbolOptions()
                        .withLatLng(
                            LatLng(
                                cottonItem.latitude ?: 0.0,
                                cottonItem.longitude ?: 0.0
                            )
                        )
                        .withIconImage(COTTON_ICON_ID)
                        .withIconSize(0.8f)

                        .withTextField("Usina")
                  //      .withDraggable(false)
//                        .withTextColor(Color.BLACK.toString())
//                        .withTextHaloColor(Color.BLACK.toString())
//                        .withTextSize(16f)
//                        //.withTextFont(arrayOf("Lato Black"))
//                        .withTextAnchor(ICON_ANCHOR_TOP)
//                        .withTextOffset(arrayOf(0f, -2.3f))
                )
            }

        cottonsSymbols
    } catch (e: Exception) {
        Log.e("MapUtil", e.toString())
    }
}


private const val COTTON_LAYER = "cotton_layer"
private const val COTTON_SOURCE = "cotton_source"
private const val COTTON_ICON_ID = "cotton_icon"

private fun createCottonSymbolLayer(style: Style, context: Context) {
    val drawable: Drawable? =
        ContextCompat.getDrawable(
            context,
            com.mapbox.mapboxsdk.R.drawable.mapbox_marker_icon_default
        )
    val icon: Bitmap? = BitmapUtils.getBitmapFromDrawable(drawable)

    icon?.also {
        style.addImage(COTTON_ICON_ID, icon)
//        val symbolLayer = SymbolLayer(COTTON_LAYER, COTTON_SOURCE)
//        symbolLayer.setProperties(
//            PropertyFactory.iconImage(COTTON_ICON_ID)
//        )
//        style.addLayer(symbolLayer)
    }
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