package tech.DevAsh.keyOS.Api.Request

import com.google.gson.annotations.SerializedName
import tech.DevAsh.keyOS.Database.User

class SetPolicyData(
        @SerializedName("id") var id:String,
        @SerializedName("policyData") var policyData:User)

