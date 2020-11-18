package tech.DevAsh.keyOS.Database;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import tech.DevAsh.KeyOS.Database.UserContext;
import tech.DevAsh.Launcher.preferences.AppsAdapter.App;
import tech.DevAsh.keyOS.Config.WebFilter;


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



    public User(RealmList<Apps> allowedApps,
                RealmList<Apps> allowedServices,
                BasicSettings basicSettings,
                WebFilterDB webFilter,
                Calls calls,
                String recoveryEmail,
                String password
    ) {
        this.allowedApps = allowedApps;
        this.allowedServices = allowedServices;
        this.basicSettings = basicSettings;
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


      static public void getUsers(){
            try {
                User user =  Realm
                        .getDefaultInstance()
                        .where(User.class)
                        .findFirst();

                UserContext.INSTANCE.setUser(Realm.getDefaultInstance()
                        .copyFromRealm(user)
                );
            } catch (Throwable throwable){
                Realm.getDefaultInstance().executeTransactionAsync(
                        realm -> {
                            UserContext.INSTANCE.setUser(new User(
                                    new RealmList<>(),
                                    new RealmList<>(new Apps("android"),new Apps("com.android.incallui")),
                                    new BasicSettings(),
                                    new WebFilterDB(false,false,false,new RealmList<>(),new RealmList<>(),false),
                                    new Calls(),
                                    "",
                                    "1234"));
                            realm.insertOrUpdate(Objects.requireNonNull(UserContext.INSTANCE.getUser()));
                            UserContext.INSTANCE.setUser(Realm.getDefaultInstance()
                                    .copyFromRealm(Objects.requireNonNull(
                                            Realm.getDefaultInstance().where(User.class)
                                                    .findFirst())));
                        }

                );
            }
        }

}
