package tech.DevAsh.keyOS.Database;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    public Boolean isDisableCamera = false;

    public BasicSettings(String wifi, String orientation, String bluetooth, String sound, Boolean notificationPanel,Boolean isDisableCamera) {
        this.wifi = wifi;
        this.orientation = orientation;
        this.bluetooth = bluetooth;
        this.sound = sound;
        this.notificationPanel = notificationPanel;
        this.isDisableCamera = isDisableCamera;
    }

    public BasicSettings(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicSettings that = (BasicSettings) o;
        return Objects.equals(wifi, that.wifi) &&
                Objects.equals(orientation, that.orientation) &&
                Objects.equals(bluetooth, that.bluetooth) &&
                Objects.equals(sound, that.sound) &&
                Objects.equals(notificationPanel, that.notificationPanel) &&
                Objects.equals(isDisableCamera, that.isDisableCamera);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(wifi, orientation, bluetooth, sound, notificationPanel, isDisableCamera);
    }
}
