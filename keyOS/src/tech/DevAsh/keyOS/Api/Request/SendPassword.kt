package tech.DevAsh.keyOS.Api.Request

import com.google.gson.annotations.SerializedName

class SendPassword{
    @SerializedName("email") var email:String?=null
    @SerializedName("password") var password:String?=null
}