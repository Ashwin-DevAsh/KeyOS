package tech.DevAsh.keyOS.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import tech.DevAsh.keyOS.Api.Request.DonationInfo
import tech.DevAsh.keyOS.Api.Request.LaunchedInfo
import tech.DevAsh.keyOS.Api.Response.BasicResponse
import tech.DevAsh.keyOS.Api.Response.DonationResponse

interface IDonationService {
    @POST("createOrder")
    fun createOrder(@Body donationInfo: DonationInfo): Call<DonationResponse>?
}