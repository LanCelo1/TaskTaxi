package uz.gita.tasktaxi.presentation.presenter

import androidx.lifecycle.LiveData
import uz.gita.tasktaxi.data.model.DriverState

interface MainScreenVM {
    val changeZoomLevelLiveData : LiveData<Double>
    val changeDriverStateLiveData : LiveData<DriverState>
    fun zoomIn(zoomLevel : Double)
    fun zoomOut(zoomLevel : Double)
    fun clickFreeDriverStateLayer()
    fun clickBusyDriverStateLayer()
}