package tech.DevAsh.keyOS.Api.Response
import com.google.gson.annotations.SerializedName


class DonationResponse {
    @SerializedName("orderID") var orderID:String?=null
    @SerializedName("keyID") var keyID:String?=null
}