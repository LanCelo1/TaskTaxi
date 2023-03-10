package uz.gita.tasktaxi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationEntity (
    @PrimaryKey(autoGenerate = true) val id : Int? = null,
    val latitude : String,
    val longitude : String,
        )