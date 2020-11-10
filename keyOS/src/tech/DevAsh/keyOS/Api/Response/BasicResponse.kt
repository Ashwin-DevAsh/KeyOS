package tech.DevAsh.keyOS.Api.Response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
class BasicResponse {
    @SerializedName("result") var result:String?=null
}