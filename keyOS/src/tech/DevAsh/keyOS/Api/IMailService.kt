package tech.DevAsh.keyOS.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import tech.DevAsh.keyOS.Api.Request.*
import tech.DevAsh.keyOS.Api.Response.BasicResponse


interface IMailService {

    @POST("v1/getEmailVerification")
    fun emailVerification(@Body emailVerification: EmailVerification): Call<BasicResponse>?

    @POST("v1/sendPassword")
    fun sendPassword(@Body sendPassword: SendPassword): Call<BasicResponse>?

    @POST("v1/newInstallAlert")
    fun newInstall(@Body deviceInfo: DeviceInfo):Call<BasicResponse>?

    @POST("v1/proApkDownloadAlert")
    fun proApkDownloadAlert(@Body deviceInfo: DeviceInfo):Call<BasicResponse>?

    @POST("v1/userLaunchedAlert")
    fun userLaunched(@Body launchedInfo: LaunchedInfo):Call<BasicResponse>?

    @POST("v1/crashAlert")
    fun crashReport(@Body crashInfo: CrashInfo):Call<BasicResponse>?

}