package tech.DevAsh.keyOS

import dagger.Component
import tech.DevAsh.KeyOS.Config.Password
import tech.DevAsh.keyOS.Api.ApiModule
import tech.DevAsh.keyOS.Api.IMailService
import javax.inject.Singleton


@Component(modules = [ApiModule::class])
@Singleton
interface ApplicationComponents {

    fun getMailService():IMailService
    fun inject(password: Password)
    fun inject(kioskApp: KioskApp)

}