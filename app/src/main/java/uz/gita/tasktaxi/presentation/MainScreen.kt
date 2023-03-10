package uz.gita.tasktaxi.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import uz.gita.tasktaxi.MainActivity
import uz.gita.tasktaxi.R
import uz.gita.tasktaxi.data.model.ServiceAction
import uz.gita.tasktaxi.databinding.ScreenMainBinding
import uz.gita.tasktaxi.presentation.presenter.MainScreenVM
import uz.gita.tasktaxi.presentation.presenter.MainScreenVMImpl
import uz.gita.tasktaxi.service.LocationService
import uz.gita.tasktaxi.utils.Constant

@AndroidEntryPoint
class MainScreen : Fragment(R.layout.screen_main) {
    private var _binding: ScreenMainBinding? = null
    private val binding get() = _binding!!
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(it)
    }
    private val viewModel: MainScreenVM by viewModels<MainScreenVMImpl>()
    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {

        }
    }
    private val requestObserver = Observer<Unit> {
        onMapReady()
        startLocationService(ServiceAction.START)
    }
    private val changeZoomObserver = Observer<Double> {
        binding.mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(it)
                .build()
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = ScreenMainBinding.bind(view)

        initView()
        initListeners()
        setUpObservers()

        (requireActivity() as MainActivity).requestPermission()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun setUpObservers() {
        Constant.requestLiveData.observe(this, requestObserver)
        viewModel.changeZoomLevelLiveData.observe(this, changeZoomObserver)
    }

    private fun initListeners()= with(binding){
        btnLocation.setOnClickListener {
            (requireActivity() as MainActivity).requestPermission()
        }
        btnThunder.setOnClickListener {
            startLocationService(ServiceAction.STOP)
        }
        btnPlus.setOnClickListener {
            val currentZoomLevel = mapView.getMapboxMap().cameraState.zoom
            viewModel.zoomIn(currentZoomLevel)
        }
        btnMinus.setOnClickListener {
            val currentZoomLevel = mapView.getMapboxMap().cameraState.zoom
            viewModel.zoomOut(currentZoomLevel)
        }
        btnMenu.setOnClickListener{
            (requireActivity() as MainActivity).openDrawer()
        }
    }

    private fun initView() = with(binding){
//        binding.mapView.getMapboxMap().loadStyleUri(Style.DARK)
        mapView.scalebar.enabled = false
        mapView.compass.enabled = false
    }


    @SuppressLint("Lifecycle")
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    @SuppressLint("Lifecycle")
    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    @SuppressLint("Lifecycle")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    private fun onMapReady() = with(binding) {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }

    private fun setupGesturesListener() {
        binding.mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = binding.mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage =
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_car
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
    }


    private fun onCameraTrackingDismissed()  {
        binding.apply {

            Toast.makeText(requireContext(), "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
            mapView.location
                .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            mapView.location
                .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            mapView.gestures.removeOnMoveListener(onMoveListener)
        }
    }


    override fun onDestroyView() {
        binding.apply {
            super.onDestroyView()
            mapView.location
                .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            mapView.location
                .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            mapView.gestures.removeOnMoveListener(onMoveListener)
            _binding = null
        }
    }

    private fun startLocationService(serviceAction: ServiceAction) {
        Intent(requireActivity().applicationContext, LocationService::class.java).apply {
            action = serviceAction.name
            requireActivity().startService(this)
        }
    }
}