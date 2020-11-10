package tech.DevAsh.keyOS.Api.Request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class EmailVerification {
    @SerializedName("email") var email:String?=null
    @SerializedName("otp") var otp:String?=null
}