package uz.gita.tasktaxi.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.gita.tasktaxi.service.client.DefaultLocationClient
import uz.gita.tasktaxi.service.client.LocationClient
import uz.gita.tasktaxi.data.local.AppDatabase
import uz.gita.tasktaxi.data.local.LocationDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "Location - database").build()
    }

    @Provides
    @Singleton
    fun provideDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }

    @Provides
    @Singleton
    fun provideFusedLocation(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provide(
        @ApplicationContext context: Context,
        client: FusedLocationProviderClient
    ): LocationClient {
        return DefaultLocationClient(context, client)
    }


}