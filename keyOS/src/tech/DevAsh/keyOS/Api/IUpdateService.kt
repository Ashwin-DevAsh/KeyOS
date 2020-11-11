package tech.DevAsh.keyOS.Api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import tech.DevAsh.keyOS.Database.User
import java.io.InputStream

interface IUpdateService {
    @GET("download")
    @Streaming
    fun update(): Call<ResponseBody>?
}