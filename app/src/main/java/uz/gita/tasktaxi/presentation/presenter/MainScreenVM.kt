package uz.gita.tasktaxi.presentation.presenter

import androidx.lifecycle.LiveData

interface MainScreenVM {
    val changeZoomLevelLiveData : LiveData<Double>
    fun zoomIn(zoomLevel : Double)
    fun zoomOut(zoomLevel : Double)
}