package uz.gita.tasktaxi.presentation.presenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainScreenVMImpl @Inject constructor() : MainScreenVM, ViewModel() {
    private val intervalZoomLevel = 0.5
    private var _changeZoomLevelLiveData = MutableLiveData<Double>()
    override val changeZoomLevelLiveData: LiveData<Double> = _changeZoomLevelLiveData

    override fun zoomIn(zoomLevel: Double) {
        _changeZoomLevelLiveData.value = zoomLevel + intervalZoomLevel
    }

    override fun zoomOut(zoomLevel: Double) {
        _changeZoomLevelLiveData.value = zoomLevel - intervalZoomLevel
    }


}