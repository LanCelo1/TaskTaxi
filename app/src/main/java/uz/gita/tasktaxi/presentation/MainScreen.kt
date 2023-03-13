package uz.gita.tasktaxi.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import uz.gita.tasktaxi.MainActivity
import uz.gita.tasktaxi.R
import uz.gita.tasktaxi.data.model.DriverState
import uz.gita.tasktaxi.data.model.ServiceAction
import uz.gita.tasktaxi.databinding.ScreenMainBinding
import uz.gita.tasktaxi.presentation.presenter.MainScreenVM
import uz.gita.tasktaxi.presentation.presenter.MainScreenVMImpl
import uz.gita.tasktaxi.service.LocationService
import uz.gita.tasktaxi.utils.*


@AndroidEntryPoint
class MainScreen : Fragment(R.layout.screen_main) {
    private var _binding: ScreenMainBinding? = null
    private val binding get() = _binding!!
    private var isChangePositionListenerWorking = false
    private var isDark = false
    private var isManualRequest = false
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        if (!isManualRequest) {
            binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).zoom(16.0).build())
        } else {
            if (isChangePositionListenerWorking) return@OnIndicatorPositionChangedListener
            binding.mapView.getMapboxMap().easeTo(
                cameraOptions {
                    center(it)
                    zoom(16.0)
                }, MapAnimationOptions.mapAnimationOptions {
                    duration(4_000)
                }
            )
            isChangePositionListenerWorking = true
        }
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
        checkTurnOnLocation {
            onMapReady()
            if (!Constant.isWorkingService) startLocationService()
            Constant.isWorkingService = true
            isChangePositionListenerWorking = false
        }
    }
    private val changeZoomObserver = Observer<Double> {
        binding.mapView.getMapboxMap().easeTo(
            cameraOptions {
                zoom(it)
            }, MapAnimationOptions.mapAnimationOptions {
                duration(500)
            }
        )
    }

    private val changeDriverStateLayerObserver = Observer<DriverState> {
        if (it == DriverState.FREE) {
            binding.layerFree.setBackgroundResource(R.drawable.bg_board_left_clicked)
            binding.txtFree.setTextColor(Color.WHITE)
            binding.layerBusy.setBackgroundResource(R.drawable.bg_board_right)
            binding.txtBusy.setTextColor(Color.BLACK)
        }else{
            binding.layerBusy.setBackgroundResource(R.drawable.bg_board_right_clicked)
            binding.txtBusy.setTextColor(Color.WHITE)
            binding.layerFree.setBackgroundResource(R.drawable.bg_board_left)
            binding.txtFree.setTextColor(Color.BLACK)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = ScreenMainBinding.bind(view)

        initView()
        loadData()
        initListeners()
        setUpObservers()

        (requireActivity() as MainActivity).requestPermission()
    }


    private fun loadData() {
        isManualRequest = false
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun setUpObservers() {
        Constant.requestLiveData.observe(viewLifecycleOwner, requestObserver)
        viewModel.changeZoomLevelLiveData.observe(this, changeZoomObserver)
        viewModel.changeDriverStateLiveData.observe(viewLifecycleOwner, changeDriverStateLayerObserver)
    }

    private fun initListeners() = with(binding) {
        btnLocation.setOnClickListener {
            isManualRequest = true
            (requireActivity() as MainActivity).requestPermission()
        }
        btnThunder.setOnClickListener {

        }
        btnPlus.setOnClickListener {
            val currentZoomLevel = mapView.getMapboxMap().cameraState.zoom
            viewModel.zoomIn(currentZoomLevel)
        }
        btnMinus.setOnClickListener {
            val currentZoomLevel = mapView.getMapboxMap().cameraState.zoom
            viewModel.zoomOut(currentZoomLevel)
        }
        btnMenu.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
        txtBusy.setOnClickListener {
            viewModel.clickBusyDriverStateLayer()
        }
        txtFree.setOnClickListener {
            viewModel.clickFreeDriverStateLayer()
        }
    }

    private fun initView() = with(binding) {
        if (isDarkMapTheme())
            changeMapTheme(Style.DARK)
        else
            changeMapTheme(Style.TRAFFIC_DAY)
        mapView.scalebar.enabled = false
        mapView.compass.enabled = false
        mapView.logo.enabled = false
        mapView.attribution.enabled = false
        requireActivity().makeStatusBarTransparent()
        constraintLayout.setPadding(0, getStatusBarHeight(), 0, getNavigationBarHeight() / 2)
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
        mapView.getMapboxMap().loadStyleUri(
            if (isDarkMapTheme()) Style.TRAFFIC_NIGHT else Style.TRAFFIC_DAY
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
                topImage =
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_car_2
                ),
                bearingImage =
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.im_car_2x
                ),
                shadowImage =
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_car_2
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


    private fun onCameraTrackingDismissed() {
        binding.apply {
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

    private fun startLocationService(serviceAction: ServiceAction = ServiceAction.START) {
        Intent(requireActivity().applicationContext, LocationService::class.java).apply {
            action = serviceAction.name
            requireActivity().startService(this)
        }
    }

    private fun checkTurnOnLocation(block: () -> Unit) {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGpsEnabled) {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("GPS is settings")
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")
            alertDialog.setPositiveButton(
                "Settings"
            ) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                requireContext().startActivity(intent)
            }
            alertDialog.setNegativeButton(
                "Cancel"
            ) { dialog, _ -> dialog.cancel() }
            alertDialog.show()
            return
        }
        block()
    }

    private fun isDarkMapTheme(): Boolean {
        when (requireContext().resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                isDark = true
                return true
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                isDark = false
                return false
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                return false
            }
        }
        return false
    }

    private fun changeMapTheme(style: String) {
        binding.mapView.getMapboxMap().loadStyleUri(style)
    }
}