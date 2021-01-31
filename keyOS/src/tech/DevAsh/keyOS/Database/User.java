package tech.DevAsh.keyOS.Database;

import android.content.Context;
import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import java.util.Objects;
import tech.DevAsh.keyOS.Helpers.AlertDeveloper;


@Keep
public class User extends RealmObject {


    @SerializedName("allowedApps")
    public RealmList<Apps> allowedApps = new RealmList();

    @SerializedName("editedApps")
    public RealmList<Apps> editedApps = new RealmList();

    @SerializedName("allowedServices")
    public RealmList<Apps> allowedServices = new RealmList<>(new Apps("android")) ;

    @SerializedName("basicSettings")
    public BasicSettings basicSettings;

    @SerializedName("singleApp")
    public Apps singleApp;

    @SerializedName("calls")
    public Calls calls;

    @SerializedName("recoveryEmail")
    public String recoveryEmail;

    @SerializedName("password")
    public String password;

    @SerializedName("webFilter")
    public WebFilterDB webFilter = new WebFilterDB();

    @SerializedName("isEndUserLicenceAgreementDone")
    public boolean isEndUserLicenceAgreementDone = false;

    @SerializedName("allowedPlugins")
    public RealmList<Plugins> allowedPlugins = new RealmList();

    @SerializedName("shouldShowSettingsIcon")
    public Boolean shouldShowSettingsIcon = false;






    public User(RealmList<Apps> allowedApps,
                RealmList<Apps> allowedServices,
                RealmList<Plugins> allowedPlugins,
                boolean shouldShowSettingsIcon,
                BasicSettings basicSettings,
                WebFilterDB webFilter,
                Calls calls,
                String recoveryEmail,
                String password
    ) {
        this.allowedApps = allowedApps;
        this.allowedServices = allowedServices;
        this.basicSettings = basicSettings;
        this.allowedPlugins = allowedPlugins;
        this.shouldShowSettingsIcon = shouldShowSettingsIcon;
        this.calls = calls;
        this.webFilter = webFilter;
        this.recoveryEmail = recoveryEmail;
        this.password = password;
    }

    public User(){}


    @Override
    public boolean equals(Object o) {
       return false;
    }


    @Override
    public String toString() {
        return "User{" +
                "allowedApps=" + allowedApps +
                ", allowedServices=" + allowedServices +
                ", basicSettings=" + basicSettings +
                ", calls=" + calls +
                ", recoveryEmail='" + recoveryEmail + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static User user = null;

    static public void getUsers(Context context){
            try {
                 user = Realm.getDefaultInstance().copyFromRealm(Objects.requireNonNull(Realm
                         .getDefaultInstance()
                         .where(User.class)
                         .findFirst()));
                 if(user==null){
                     throw new Exception("User Null");
                 }

            } catch (Throwable throwable){
                Realm.getDefaultInstance().executeTransactionAsync(
                        realm -> {
                           user = (new User(
                                    new RealmList<>(),
                                    new RealmList<>(new Apps("android"),new Apps("com.android.incallui")),
                                    new RealmList<>(),
                                    false,
                                    new BasicSettings(),
                                    new WebFilterDB(false,false,false,new RealmList<>(),new RealmList<>(),false),
                                    new Calls(),
                                    "",
                                    "1234"));
                            realm.insertOrUpdate(Objects.requireNonNull(user));
                            user = (Realm.getDefaultInstance()
                                    .copyFromRealm(Objects.requireNonNull(
                                            Realm.getDefaultInstance().where(User.class)
                                                    .findFirst())));
                        }

                );
                AlertDeveloper.INSTANCE.sendNewInstallAlert(context);
            }
        }
}
