package tech.DevAsh.keyOS.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import tech.DevAsh.keyOS.Api.Request.EmailVerification
import tech.DevAsh.keyOS.Api.Request.SendPassword
import tech.DevAsh.keyOS.Api.Request.SetPolicyData
import tech.DevAsh.keyOS.Api.Response.BasicResponse
import tech.DevAsh.keyOS.Database.User

interface IQRCodeService {
//    @POST("setPolicyData")
//    fun setPolicyData(@Body setPolicyData: SetPolicyData): Call<BasicResponse>?

    @GET("getPolicyData/{id}")
    fun getPolicyData(@Path("id") id: String): Call<User>?
}