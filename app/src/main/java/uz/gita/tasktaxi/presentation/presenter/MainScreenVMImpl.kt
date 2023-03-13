package uz.gita.tasktaxi.presentation.presenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uz.gita.tasktaxi.data.model.DriverState
import javax.inject.Inject

@HiltViewModel
class MainScreenVMImpl @Inject constructor() : MainScreenVM, ViewModel() {
    private val intervalZoomLevel = 1.0
    private var isDriverBusy = false
    private var _changeZoomLevelLiveData = MutableLiveData<Double>()
    override val changeZoomLevelLiveData: LiveData<Double> = _changeZoomLevelLiveData
    private var _changeDriverStateLiveData = MutableLiveData<DriverState>()
    override val changeDriverStateLiveData: LiveData<DriverState> = _changeDriverStateLiveData

    override fun zoomIn(zoomLevel: Double) {
        _changeZoomLevelLiveData.value = zoomLevel + intervalZoomLevel
    }

    override fun zoomOut(zoomLevel: Double) {
        _changeZoomLevelLiveData.value = zoomLevel - intervalZoomLevel
    }

    override fun clickFreeDriverStateLayer() {
        if (!isDriverBusy) return
        isDriverBusy = false
        _changeDriverStateLiveData.value = DriverState.FREE
    }

    override fun clickBusyDriverStateLayer() {
        if (isDriverBusy) return
        isDriverBusy = true
        _changeDriverStateLiveData.value = DriverState.BUSY
    }
}