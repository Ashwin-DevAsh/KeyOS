package tech.DevAsh.keyOS.Database;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import java.util.Arrays;
import java.util.List;

public class BasicSettings extends RealmObject {


    @Ignore
    public static String AlwaysON = "Always on";

    @Ignore
    public static String AlwaysOFF = "Always off";

    @Ignore
    public static String DontCare = "Don't care";

    @Ignore
    public static List<String> options = Arrays.asList(DontCare,AlwaysON,AlwaysOFF);



    public String wifi = DontCare;
    public String hotspot = DontCare;
    public String bluetooth = DontCare;
    public String mobileData = DontCare;
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
