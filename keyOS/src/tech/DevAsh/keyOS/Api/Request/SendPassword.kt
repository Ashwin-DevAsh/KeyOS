package tech.DevAsh.keyOS.Api.Request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class SendPassword{
    @SerializedName("email") var email:String?=null
    @SerializedName("password") var password:String?=null
}