package uz.gita.tasktaxi.data.repository

import uz.gita.tasktaxi.data.model.LocationData

interface AppRepository {
    suspend fun insertLocation(locationData: LocationData)
}