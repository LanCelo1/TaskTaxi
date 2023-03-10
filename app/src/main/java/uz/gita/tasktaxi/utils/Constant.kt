package uz.gita.tasktaxi.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object Constant {

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    var _requestLiveData = MutableLiveData<Unit>()
    val requestLiveData : LiveData<Unit> = _requestLiveData

}