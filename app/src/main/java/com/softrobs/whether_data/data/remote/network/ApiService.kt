package com.softrobs.whether_data.data.remote.network

import com.softrobs.whether_data.data.remote.response.ResponseFromForecastModel
import com.softrobs.whether_data.utils.FORECAST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET(FORECAST)
    suspend fun getWetherForecast(@Query("lat") lat:Double, @Query("lon")
    lon:Double, @Query("apiKey") apiKey:String,@Query("mode") mode:String,
    @Query("units") units:String):Response<ResponseFromForecastModel>
}