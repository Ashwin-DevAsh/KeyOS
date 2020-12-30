package tech.DevAsh.keyOS.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import tech.DevAsh.keyOS.Api.Request.EmailVerification
import tech.DevAsh.keyOS.Api.Request.DeviceInfo
import tech.DevAsh.keyOS.Api.Request.LaunchedInfo
import tech.DevAsh.keyOS.Api.Request.SendPassword
import tech.DevAsh.keyOS.Api.Response.BasicResponse


interface IMailService {

    @POST("getEmailVerification")
    fun emailVerification(@Body emailVerification: EmailVerification): Call<BasicResponse>?

    @POST("sendPassword")
    fun sendPassword(@Body sendPassword: SendPassword): Call<BasicResponse>?

    @POST("newInstallAlert")
    fun newInstall(@Body deviceInfo: DeviceInfo):Call<BasicResponse>?

    @POST("proApkDownloadAlert")
    fun proApkDownloadAlert(@Body deviceInfo: DeviceInfo):Call<BasicResponse>?

    @POST("userLaunchedAlert")
    fun userLaunched(@Body launchedInfo: LaunchedInfo):Call<BasicResponse>?

}