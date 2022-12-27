package com.softrobs.whether_data.di

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.softrobs.whether_data.data.remote.network.ApiService
import com.softrobs.whether_data.utils.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Singleton
    @Provides
    fun provideServices():ApiService{
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        HttpLoggingInterceptor()
                            .apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            })
                    .callTimeout(4, TimeUnit.MINUTES)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(ApiService::class.java)
    }
}