package tech.DevAsh.keyOS.Database;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Keep
public class Apps extends RealmObject {



    public static List<Apps> allApps= new ArrayList <>();
    public static List<Apps> allService = new ArrayList <>();
    public static List<String> exceptions = Arrays.asList("com.android.settings.AllowBindAppWidgetActivity", "android.app.Dialog");


    @SerializedName("packageName")
    public String packageName;

    @SerializedName("hourPerDay")
    public String hourPerDay = "24:00";

    @SerializedName("blockedActivities")
    public RealmList<String> blockedActivities = new RealmList();

    @SerializedName("hideShortcut")
    public Boolean hideShortcut = false;


    @Ignore
    public Drawable icon;
    @Ignore
    public String appName;
    @Ignore
    public PackageInfo packageInfo;

    public Apps(String packageName, Drawable icon, String appName,PackageInfo packageInfo) {
        this.packageName = packageName;
        this.icon = icon;
        this.appName = appName;
        this.packageInfo = packageInfo;
    }

    public Apps(String packageName, Drawable icon, String appName) {
        this.packageName = packageName;
        this.icon = icon;
        this.appName = appName;
    }




    public Apps(){}


    public Apps(String packageName){
        this.packageName=packageName;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof String){
          return  this.packageName == o;
        }
        Apps that = (Apps) o;
        return Objects.equals(packageName, that.packageName);
    }

    public void update(Apps app){
        this.blockedActivities = app.blockedActivities;
        this.hourPerDay = app.hourPerDay;
    }

    @NotNull
    @Override
    public String toString() {
        return "Apps{" +
                "packageName='" + packageName + '\'' +
                ", hourPerDay='" + hourPerDay + '\'' +
                ", blockedActivities=" + blockedActivities +
                ", icon=" + icon +
                ", appName='" + appName + '\'' +
                ", packageInfo=" + packageInfo +
                '}';
    }
}
