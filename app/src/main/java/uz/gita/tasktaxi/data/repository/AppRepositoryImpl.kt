package uz.gita.tasktaxi.data.repository

import uz.gita.tasktaxi.data.local.LocationDao
import uz.gita.tasktaxi.data.model.LocationData
import uz.gita.tasktaxi.utils.Mapper
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao
) : AppRepository {
    override suspend fun insertLocation(locationData: LocationData) {
        val location = Mapper.run {
            locationData.convertToLocationEntity()
        }
        locationDao.insert(location)
    }
}