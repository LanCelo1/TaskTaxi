package uz.gita.tasktaxi.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.gita.tasktaxi.data.repository.AppRepository
import uz.gita.tasktaxi.data.repository.AppRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppService {
    @Binds
    @Singleton
    fun bindRepository(repositoryImpl: AppRepositoryImpl): AppRepository



}