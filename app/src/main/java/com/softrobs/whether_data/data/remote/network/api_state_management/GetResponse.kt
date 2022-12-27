package com.softrobs.whether_data.data.remote.network.api_state_management

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.Response

object GetResponse {
    fun <T> fromFlow(safeAPICall: suspend () -> Response<T>): Flow<ApiState<T>> = flow {
        emit(ApiState.LOADING)
        try {
            val response = safeAPICall()


            Log.d("EXCEPTION_FROM_LOGIN_API" , response.isSuccessful.toString())
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(ApiState.SUCCESS(it))
                }
            } else {
                response.errorBody()?.let { error ->
                    error.close()
                    emit(ApiState.ERROR(message = error.toString(),
                        isNetworkError = false,
                        errorCode = response.code(),
                        errorBody = response.errorBody()))
                }
            }
        }catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> {
                    Log.d("EXCEPTION_FROM_LOGIN_API" , throwable.code().toString())

                    ApiState.ERROR(message = throwable.message!!,
                        isNetworkError = false,
                        errorCode = throwable.code(),
                        errorBody = throwable.response()?.errorBody())
                }
                else -> {
                    Log.d("EXCEPTION_FROM_LOGIN_API" , throwable.message!!.toString())

                    ApiState.ERROR(message = throwable.message!!,
                        isNetworkError = true,
                        errorCode = null,
                        errorBody = null)
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}