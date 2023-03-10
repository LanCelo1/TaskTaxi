package uz.gita.tasktaxi.utils

import uz.gita.tasktaxi.data.local.LocationEntity
import uz.gita.tasktaxi.data.model.LocationData

object Mapper {
    fun LocationData.convertToLocationEntity() = LocationEntity(
        latitude = this.latitude,
        longitude = this.longitude
    )
}