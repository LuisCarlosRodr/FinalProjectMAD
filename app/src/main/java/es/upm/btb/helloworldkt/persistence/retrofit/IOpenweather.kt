package es.upm.btb.helloworldkt.persistence.retrofit

import es.upm.btb.helloworldkt.data.WeatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IOpenWeather {
    @GET("data/2.5/find")
    fun getWeatherData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("cnt") count: Int,
        @Query("appid") apiKey: String
    ): Call<WeatherData>
}
