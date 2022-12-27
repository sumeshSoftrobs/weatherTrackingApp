package com.softrobs.whether_data.data.remote.response

data class ResponseFromForecastModel(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<>,
    val message: Int
)