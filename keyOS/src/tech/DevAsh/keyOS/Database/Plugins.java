package tech.DevAsh.keyOS.Database;

import android.provider.Settings;
import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import io.realm.RealmObject;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Keep
public class Plugins extends RealmObject {


    static public List<Plugins> allPlugins = Arrays
                .asList(
                        new Plugins("Wifi", Settings.ACTION_WIFI_SETTINGS),
                        new Plugins("Bluetooth",Settings.ACTION_BLUETOOTH_SETTINGS),
                        new Plugins("Sound",Settings.ACTION_SOUND_SETTINGS),
                        new Plugins("Airplane Mode",Settings.ACTION_AIRPLANE_MODE_SETTINGS),
                        new Plugins("Mobile Data",Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
                        new Plugins("GPS",Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        new Plugins("Vpn",Settings.ACTION_VPN_SETTINGS)
                );


    @SerializedName("pluginName")
    public String pluginName;

    @SerializedName("packageName")
    public String packageName;

    @SerializedName("className")
    public String className;



    public Plugins(String pluginName, String packageName,String className) {
        this.pluginName = pluginName;
        this.packageName = packageName;
        this.className = className;
    }

    public Plugins(String pluginName,String packageName){
        this.pluginName = pluginName;
        this.packageName = packageName;
    }

    public Plugins(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Plugins plugins = (Plugins) o;
        try{
            return className.contains(plugins.className) || plugins.className.contains(className);
        }catch( Throwable throwable){
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

    @Override
    public String toString() {
        return "Plugins{" +
                "pluginName='" + pluginName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
