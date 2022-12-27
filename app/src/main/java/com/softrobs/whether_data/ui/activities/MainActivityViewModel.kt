package com.softrobs.whether_data.ui.activities

import android.util.Log
import android.util.Log.e
import android.util.Log.i
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softrobs.whether_data.data.remote.network.ApiService
import com.softrobs.whether_data.data.remote.network.NetworkRepository
import com.softrobs.whether_data.data.remote.network.api_state_management.ApiState
import com.softrobs.whether_data.data.remote.response.ResponseFromForecastModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber.e
import java.util.logging.Logger
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val repository: NetworkRepository) : ViewModel(){
    private val wetherDataFlow = Channel<ApiState<ResponseFromForecastModel>>(Channel.BUFFERED)
    val name = "Sumesh"
    val observeWeatherData = wetherDataFlow.receiveAsFlow()

    fun invokeWetherForecastCall(lat:Double,lon:Double,apiKey:String,mode:String,units:String){
        Log.d("EXCEPTION_FROM_LOGIN_API" , "Checking")

        viewModelScope.launch{
            repository.getWetherForecastData(lat,lon,apiKey,mode,units)
                .collect{response->
                    wetherDataFlow.send(response)
                    Log.d("EXCEPTION_FROM_LOGIN_API" , response.toString())

                }
        }

    }
}