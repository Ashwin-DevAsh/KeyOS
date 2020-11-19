package tech.DevAsh.keyOS.Api.Request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import tech.DevAsh.keyOS.Database.User

@Keep
class SetPolicyData(
        @SerializedName("id") var id:String,
        @SerializedName("policyData") var policyData:User
                   )

