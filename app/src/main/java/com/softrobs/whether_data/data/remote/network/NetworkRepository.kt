package com.softrobs.whether_data.data.remote.network

import com.softrobs.whether_data.data.remote.network.api_state_management.ApiState
import com.softrobs.whether_data.data.remote.network.api_state_management.GetResponse
import com.softrobs.whether_data.data.remote.response.ResponseFromForecastModel
import java.util.concurrent.Flow
import javax.inject.Inject

class NetworkRepository @Inject constructor(val service: ApiService) {

    suspend fun getWetherForecastData(lat:Double,lon: Double,apiKey:String,mode:String,units:String): kotlinx.coroutines.flow.Flow<ApiState<ResponseFromForecastModel>>{
        return GetResponse.fromFlow {
            service.getWetherForecast(lat,lon,apiKey,mode,units)
        }
    }
}