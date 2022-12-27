package com.softrobs.whether_data.data.remote.network.api_state_management

import okhttp3.ResponseBody

sealed class ApiState<out T>{
    data class SUCCESS<out R>(val getResponse: R) : ApiState<R>()
    data class ERROR(val message: String, val isNetworkError: Boolean,
                     val errorCode: Int?,
                     val errorBody: ResponseBody?) : ApiState<Nothing>()
    object LOADING : ApiState<Nothing>()
}
