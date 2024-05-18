package es.upm.btb.helloworldkt.persistence.retrofit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.upm.btb.helloworldkt.R
import es.upm.btb.helloworldkt.data.WeatherItem

class WeatherAdapter(private var weatherList: List<WeatherItem>) : RecyclerView.Adapter<WeatherViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherItem = weatherList[position]
        holder.cityName.text = weatherItem.name
        holder.temperature.text = "\uD83C\uDF21\uFE0F Temperatura: ${kelvinToCelsius(weatherItem.main.temp)}ºC"
        holder.tempFL.text = "Tienes una sensación: ${tempKelvin2Icon(weatherItem.main.feelsLike)}"
        holder.tempMax.text = "Máximo: ${kelvinToCelsius(weatherItem.main.tempMax)}ºC"
        holder.tempMin.text = "Mínimo: ${kelvinToCelsius(weatherItem.main.tempMin)}ºC"
        holder.humidity.text = "Humedad: ${weatherItem.main.humidity}%"
        holder.windSpeed.text = "\uD83C\uDF2A\uFE0F ${mphToKmh(weatherItem.wind.speed)} Kmh"
        holder.weatherM.text = "${weatherItem.weather[0].main} / ${weatherItem.weather[0].description}"
        Glide.with(holder.itemView.context).load("http://openweathermap.org/img/wn/${weatherItem.weather[0].icon}.png").into(holder.icon)
    }

    override fun getItemCount() = weatherList.size

    fun updateWeatherData(newWeatherList: List<WeatherItem>) {
        weatherList = newWeatherList
        notifyDataSetChanged()
    }

    private fun kelvinToCelsius(kelvin: Double) = String.format("%.1f", kelvin - 273.15)

    private fun mphToKmh(mph: Double) = String.format("%.2f", mph * 1.60934)

    private fun tempKelvin2Icon(tempKelvin: Double): String {
        val tempCelsius = tempKelvin - 273.15
        return when {
            tempCelsius <= 10 -> "🥶" // Cold
            tempCelsius in 11.0..25.0 -> "😀" // Nice
            else -> "🥵" // Hot
        }
    }
}
