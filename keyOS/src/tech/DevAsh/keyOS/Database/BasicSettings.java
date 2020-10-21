package tech.DevAsh.keyOS.Database;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import java.util.Arrays;
import java.util.List;

public class BasicSettings extends RealmObject {


    @Ignore public static String AlwaysON = "Always on";

    @Ignore public static String AlwaysOFF = "Always off";

    @Ignore public static String DontCare = "Don't care";

    @Ignore public static String landscape = "Landscape";

    @Ignore static public String portrait = "Portrait";

    @Ignore static public String silent = "Silent";

    @Ignore static public String normal = "Normal";

    @Ignore public static List<String> options = Arrays.asList(DontCare,AlwaysON,AlwaysOFF);

    @Ignore public static List<String> orientationOptions = Arrays.asList(DontCare,portrait,landscape);

    @Ignore public static List<String> soundOptions = Arrays.asList(DontCare,normal,silent);




    public String wifi = DontCare;
    public String orientation = DontCare;
    public String bluetooth = DontCare;
    public String sound = DontCare;
    public Boolean notificationPanel = false;

    public BasicSettings(String wifi, String orientation, String bluetooth, String sound, Boolean notificationPanel) {
        this.wifi = wifi;
        this.orientation = orientation;
        this.bluetooth = bluetooth;
        this.sound = sound;
        this.notificationPanel = notificationPanel;
    }

    public BasicSettings(){}

}
