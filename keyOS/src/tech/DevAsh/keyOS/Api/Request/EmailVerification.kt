package tech.DevAsh.keyOS.Api.Request

import com.google.gson.annotations.SerializedName

class EmailVerification {
    @SerializedName("email") var email:String?=null
    @SerializedName("otp") var otp:String?=null
}