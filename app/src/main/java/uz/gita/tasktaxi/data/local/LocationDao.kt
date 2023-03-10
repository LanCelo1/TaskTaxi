package uz.gita.tasktaxi.data.local

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationEntity)
}