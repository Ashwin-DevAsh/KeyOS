package tech.DevAsh.keyOS.Database;

import io.realm.Realm;
import io.realm.Realm.Transaction;
import io.realm.RealmList;
import io.realm.RealmObject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import tech.DevAsh.KeyOS.Database.UserContext;

public class User extends RealmObject {
    public RealmList<Apps> allowedApps = new RealmList();
    public RealmList<Apps> editedApps = new RealmList();
    public RealmList<Apps> allowedServices = new RealmList() ;
    public BasicSettings basicSettings;
    public Calls calls;
    public String recoveryEmail;
    public String password;

    public User(RealmList<Apps> allowedApps,
                RealmList<Apps> allowedServices,
                BasicSettings basicSettings,
                Calls calls,
                String recoveryEmail,
                String password
    ) {
        this.allowedApps = allowedApps;
        this.allowedServices = allowedServices;
        this.basicSettings = basicSettings;
        this.calls = calls;
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
                UserContext.INSTANCE.setUser(Realm.getDefaultInstance()
                        .copyFromRealm(
                                Realm.getDefaultInstance().where(User.class).findFirst()));
            } catch (Throwable throwable){
                Realm.getDefaultInstance().executeTransactionAsync(
                        realm -> {
                            UserContext.INSTANCE.setUser(new User(
                                    new RealmList<Apps>(),
                                    new RealmList<Apps>(),
                                    new BasicSettings(),
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
