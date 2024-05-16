package es.upm.btb.helloworldkt.data

import com.google.gson.annotations.SerializedName

data class Sys(
    @SerializedName("country")
    val country: String
)