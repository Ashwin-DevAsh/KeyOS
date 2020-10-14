package tech.DevAsh.keyOS.Database;

import io.realm.RealmObject;

public class BasicSettings extends RealmObject {
    public String wifi = "Don't care";
    public String hotspot = "Don't care";
    public String bluetooth = "Don't care";
    public String mobileData = "Don't care";
    public Boolean notificationPanel = false;

    public BasicSettings(String wifi, String hotspot, String bluetooth, String mobileData, Boolean notificationPanel) {
        this.wifi = wifi;
        this.hotspot = hotspot;
        this.bluetooth = bluetooth;
        this.mobileData = mobileData;
        this.notificationPanel = notificationPanel;
    }

    public BasicSettings(){}

}
