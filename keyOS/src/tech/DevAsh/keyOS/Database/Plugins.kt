package tech.DevAsh.keyOS.Database

import android.provider.Settings
import androidx.annotation.Keep
import com.android.launcher3.R
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import java.util.*
import kotlin.collections.HashMap

@Keep
open class Plugins : RealmObject {
    @SerializedName("pluginName")
    var pluginName: String? = null

    @SerializedName("packageName")
    var packageName: String? = null

    @SerializedName("className")
    var className: String? = null

    constructor(pluginName: String?, packageName: String?, className: String?) {
        this.pluginName = pluginName
        this.packageName = packageName
        this.className = className
    }

    constructor(pluginName: String?, packageName: String?) {
        this.pluginName = pluginName
        this.packageName = packageName
    }

    constructor()

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val plugins = o as Plugins
        return try {
            className!!.contains(plugins.className!!) || plugins.className!!.contains(
                    className!!)
        } catch (throwable: Throwable) {
            false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(className)
    }

    companion object {
        var allPlugins = listOf(
                Plugins("Wifi", Settings.ACTION_WIFI_SETTINGS),
                Plugins("Bluetooth", Settings.ACTION_BLUETOOTH_SETTINGS),
                Plugins("Sound", Settings.ACTION_SOUND_SETTINGS),
                Plugins("Airplane Mode", Settings.ACTION_AIRPLANE_MODE_SETTINGS),
                Plugins("Mobile Data", Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
                Plugins("GPS", Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                Plugins("Vpn", Settings.ACTION_VPN_SETTINGS)
                       )

        var allPluginsMap = mapOf(
             "Wifi" to R.string.wifi,
             "Bluetooth" to R.string.bluetooth,
             "Sound" to R.string.sound,
             "Airplane Mode" to R.string.airplane_mode,
             "Mobile Data" to R.string.mobile_data,
             "GPS" to R.string.gps,
             "Vpn" to R.string.vpn
                                 )



    }
}