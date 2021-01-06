package tech.DevAsh.keyOS.Socket

import android.content.Context
import android.os.Handler
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import tech.DevAsh.keyOS.Api.ApiContext
import tech.DevAsh.keyOS.Helpers.KioskHelpers.AlertDeveloper

object SocketHelper {

    var socket: Socket? = null


    fun connect(context: Context){
        Handler().post {
            try {
                socket = IO.socket(ApiContext.socketServiceUrl)
                socket?.connect()
                socket?.on("connect") {
                    val data = JSONObject(mapOf("deviceID"  to AlertDeveloper.getInstallDetails(context).deviceID))
                    socket?.emit("setInfo",data)
                }
                socket?.on("disconnect"){}
            }catch (e:Throwable){}

        }
    }


}