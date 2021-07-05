package tech.DevAsh.keyOS.Api.Request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
class DonationInfo(
        @SerializedName("amount") var amount:Double,
        @SerializedName("id") var id:String,
        )