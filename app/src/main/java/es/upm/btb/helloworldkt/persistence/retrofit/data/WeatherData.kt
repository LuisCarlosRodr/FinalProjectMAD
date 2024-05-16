package es.upm.btb.helloworldkt.data

import com.google.gson.annotations.SerializedName

data class WeatherData(
    @SerializedName("message")
    val message: String,
    @SerializedName("cod")
    val cod: String,
    @SerializedName("count")
    val count: Int,
    @SerializedName("list")
    val list: List<WeatherItem>
)

