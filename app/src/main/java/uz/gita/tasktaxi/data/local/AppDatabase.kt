package uz.gita.tasktaxi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun locationDao() : LocationDao
}