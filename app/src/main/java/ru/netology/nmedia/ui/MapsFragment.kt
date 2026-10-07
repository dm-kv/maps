package ru.netology.nmedia.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.mapview.MapView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMapsBinding
import ru.netology.nmedia.domain.Marker
import ru.netology.nmedia.domain.MarkerArgs
import ru.netology.nmedia.ui.extensions.DrawableImageProvider
import ru.netology.nmedia.ui.extensions.ImageInfo
import ru.netology.nmedia.viemodel.MarkerViewModel
import java.lang.ref.WeakReference

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MarkerViewModel by activityViewModels()
    private var mapView: MapView? = null

    private var isViewAlive = false
    private var markerTapped = false


    private val tapListeners = mutableListOf<MapObjectTapListener>()

    private val myPlacemarks = mutableListOf<com.yandex.mapkit.map.MapObject>()


    private val inputListenerImpl = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            if (markerTapped) {
                markerTapped = false
                return
            }
            findNavController().navigate(R.id.edit, Bundle().apply {
                putLong("markerId", -1L)
                putDouble("latitude", point.latitude)
                putDouble("longitude", point.longitude)
            })
        }

        override fun onMapLongTap(map: Map, point: Point) {}
    }
    private val mapInputListener = WeakReference<InputListener>(inputListenerImpl)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                mapView?.let { enableUserLocation(it.mapWindow) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.map
        val yandexMap = mapView!!.mapWindow.map

        subscribeToLifecycle(mapView!!)

        binding.fabList.setOnClickListener {
            findNavController().navigate(R.id.action_maps_to_list)
        }

        binding.fabZoomIn?.setOnClickListener {
            val current = yandexMap.cameraPosition
            yandexMap.move(
                CameraPosition(current.target, current.zoom + 1F, current.azimuth, current.tilt),
                Animation(Animation.Type.SMOOTH, 0.3F),
                null
            )
        }
        binding.fabZoomOut?.setOnClickListener {
            val current = yandexMap.cameraPosition
            yandexMap.move(
                CameraPosition(current.target, current.zoom - 1F, current.azimuth, current.tilt),
                Animation(Animation.Type.SMOOTH, 0.3F),
                null
            )
        }

        viewModel.markers.observe(viewLifecycleOwner) { markers ->
            Log.d("MapsFrag", ">>> Получен список маркеров! Размер: ${markers.size}")
            renderMarkers(yandexMap, markers)
        }

        viewModel.focusMarkerId.observe(viewLifecycleOwner) { focusId ->
            if (focusId != null) {
                viewModel.markers.value?.find { it.id == focusId }?.let { marker ->
                    moveCamera(yandexMap, Point(marker.latitude, marker.longitude))
                }
                viewModel.focusConsumed()
            }
        }

        yandexMap.addInputListener(mapInputListener)

        checkPermissions()

        isViewAlive = true
    }

    private fun renderMarkers(yandexMap: Map, markers: List<Marker>) {
        if (!isViewAlive) return

        myPlacemarks.forEach { yandexMap.mapObjects.remove(it) }
        myPlacemarks.clear()
        tapListeners.clear()

        Log.d("MapsFrag", ">>> renderMarkers: получено ${markers.size} точек")

        if (markers.isEmpty()) {
            Log.w("MapsFrag", "Список маркеров пуст!")
            return
        }

        val imageProvider = DrawableImageProvider(
            requireContext(),
            ImageInfo(android.R.drawable.btn_star_big_on)
        )

        markers.forEach { marker ->
            val title = marker.title.orEmpty()

            val tapListener = object : MapObjectTapListener {
                override fun onMapObjectTap(mapObject: com.yandex.mapkit.map.MapObject, point: Point): Boolean {
                    val storedMarker = mapObject.userData as? Marker
                    if (storedMarker != null) {
                        markerTapped = true
                        findNavController().navigate(R.id.edit, Bundle().apply {
                            putLong(MarkerArgs.MARKER_ID, storedMarker.id)
                            putDouble(MarkerArgs.LATITUDE, storedMarker.latitude)
                            putDouble(MarkerArgs.LONGITUDE, storedMarker.longitude)
                        })
                    }
                    return true
                }
            }

            tapListeners.add(tapListener)

            val placemark = yandexMap.mapObjects.addPlacemark {
                it.setIcon(imageProvider)
                it.geometry = Point(marker.latitude, marker.longitude)
                it.setText(title)
                it.userData = marker
                it.addTapListener(WeakReference(tapListener))
            }
            myPlacemarks.add(placemark)
        }
    }


    private fun moveCamera(yandexMap: Map, target: Point) {
        val current = yandexMap.cameraPosition
        yandexMap.move(
            CameraPosition(target, 15F, current.azimuth, current.tilt),
            Animation(Animation.Type.SMOOTH, 1F),
            null
        )
    }

    private fun checkPermissions() {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            mapView?.let { enableUserLocation(it.mapWindow) }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableUserLocation(mapWindow: com.yandex.mapkit.map.MapWindow) {
        MapKitFactory.getInstance().createUserLocationLayer(mapWindow).apply {
            isVisible = true
            isHeadingModeActive = true
        }
    }

    private fun subscribeToLifecycle(mapView: MapView) {
        viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        MapKitFactory.getInstance().onStart()
                        mapView.onStart()
                    }
                    Lifecycle.Event.ON_STOP -> {
                        mapView.onStop()
                        MapKitFactory.getInstance().onStop()
                    }
                    Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
                    else -> Unit
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewAlive = false
        markerTapped = false
        myPlacemarks.clear()
        tapListeners.clear()
        mapView = null
        _binding = null
    }
}