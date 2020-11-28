package tech.DevAsh.keyOS

import dagger.Component
import tech.DevAsh.KeyOS.Config.Password
import tech.DevAsh.keyOS.Api.ApiModule
import tech.DevAsh.keyOS.Api.IMailService
import tech.DevAsh.keyOS.Api.IQRCodeService
import tech.DevAsh.keyOS.Config.ImportExportSettings
import tech.DevAsh.keyOS.Config.QrScanner
import javax.inject.Singleton


@Component(modules = [ApiModule::class])
@Singleton
interface ApplicationComponents {

    fun getMailService():IMailService
    fun getQrCodeService():IQRCodeService
    fun inject(password: Password)
    fun inject(kioskApp: KioskApp)
    fun inject(importExportSettings: ImportExportSettings)
    fun inject(qrScanner: QrScanner)

}