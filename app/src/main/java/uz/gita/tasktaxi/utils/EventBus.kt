package uz.gita.tasktaxi.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object EventBus {
    var _requestLiveData = MutableLiveData<Unit>()
    val requestLiveData : LiveData<Unit> = _requestLiveData

}