package tech.DevAsh.keyOS.Database;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class Apps extends RealmObject {

    public String packageName;
    public String hourPerDay;
    public String allowAfter;
    public RealmList<String> blockedActivities = new RealmList();
    public String modifiedName;
    public String modifiedIconPath;

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

    public Apps
            (String packageName,
             String hourPerDay,
             String allowAfter,
             RealmList<String> blockedActivities,
             String modifiedName,
             String modifiedIconPath
            ) {
        this.packageName = packageName;
        this.hourPerDay = hourPerDay;
        this.allowAfter = allowAfter;
        this.blockedActivities = blockedActivities;
        this.modifiedName = modifiedName;
        this.modifiedIconPath = modifiedIconPath;
    }



    public Apps(){}

    public Apps(String packageName, String hourPerDay, String startAfter) {
        this.packageName = packageName;
        this.hourPerDay = hourPerDay;
        this.allowAfter = startAfter;
    }

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
        this.allowAfter = app.allowAfter;
        this.hourPerDay = app.hourPerDay;
    }

    @NotNull
    @Override
    public String toString() {
        return "Apps{" +
                "packageName='" + packageName + '\'' +
                ", hourPerDay='" + hourPerDay + '\'' +
                ", allowAfter='" + allowAfter + '\'' +
                ", blockedActivities=" + blockedActivities +
                ", modifiedName='" + modifiedName + '\'' +
                ", modifiedIconPath='" + modifiedIconPath + '\'' +
                ", icon=" + icon +
                ", appName='" + appName + '\'' +
                ", packageInfo=" + packageInfo +
                '}';
    }
}
