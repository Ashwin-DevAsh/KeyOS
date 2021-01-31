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
                        new Plugins("wifi", Settings.ACTION_WIFI_SETTINGS),
                        new Plugins("bluetooth",Settings.ACTION_BLUETOOTH_SETTINGS)
                );


    @SerializedName("pluginName")
    public String pluginName;

    @SerializedName("packageName")
    public String packageName;

    public Plugins(String pluginName, String packageName) {
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
        return Objects.equals(packageName, plugins.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName);
    }
}
