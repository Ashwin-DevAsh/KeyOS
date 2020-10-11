package tech.DevAsh.keyOS.Database;

import io.realm.RealmObject;

public class BasicSettings extends RealmObject {
    public String wifi = "Deny";
    public String hotspot = "Deny";
    public String bluetooth = "Deny";
    public String mobileData = "Deny";
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
