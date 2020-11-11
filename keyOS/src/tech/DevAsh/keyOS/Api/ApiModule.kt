package tech.DevAsh.keyOS.Api

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
class ApiModule {

    var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(ApiContext.mailServiceUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    fun getMailService():IMailService{
        return retrofit.create(IMailService::class.java)
    }

    @Provides
    fun getQRCodeService():IQRCodeService{
        return retrofit.create(IQRCodeService::class.java)
    }

    @Provides
    fun getUpdatedApk():IUpdateService{
        return retrofit.create(IUpdateService::class.java)
    }
}