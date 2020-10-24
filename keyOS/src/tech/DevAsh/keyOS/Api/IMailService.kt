package tech.DevAsh.keyOS.Api

import dagger.Component
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import tech.DevAsh.keyOS.Api.Request.EmailVerification
import tech.DevAsh.keyOS.Api.Request.SendPassword
import tech.DevAsh.keyOS.Api.Response.BasicResponse
import javax.inject.Singleton


interface IMailService {

    @POST("getEmailVerification")
    fun emailVerification(@Body emailVerification: EmailVerification): Call<BasicResponse>?

    @POST("sendPassword")
    fun sendPassword(@Body sendPassword: SendPassword): Call<BasicResponse>?
}