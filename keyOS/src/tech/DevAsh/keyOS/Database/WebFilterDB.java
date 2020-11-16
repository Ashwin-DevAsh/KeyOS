package tech.DevAsh.keyOS.Database;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import io.realm.RealmList;
import io.realm.RealmObject;

@Keep
public class WebFilterDB extends RealmObject {
    @SerializedName("isEnabled")
    public Boolean isEnabled = false;
    @SerializedName("isWhitelistEnabled")
    public Boolean isWhitelistEnabled = false;
    @SerializedName("isBlacklistEnabled")
    public Boolean isBlacklistEnabled = false;
    @SerializedName("whitelistWebsites")
    public RealmList<String> whitelistWebsites = new RealmList();
    @SerializedName("blacklistWebsites")
    public RealmList<String> blacklistWebsites = new RealmList();
    @SerializedName("shouldBlockAdultSites")
    public Boolean shouldBlockAdultSites = false;

    @Override
    public String toString() {
        return "WebFilterDB{" +
                "isEnabled=" + isEnabled +
                ", isWhitelistEnabled=" + isWhitelistEnabled +
                ", isBlacklistEnabled=" + isBlacklistEnabled +
                ", whitelistWebsites=" + whitelistWebsites +
                ", blacklistWebsites=" + blacklistWebsites +
                ", shouldBlockAdultSites=" + shouldBlockAdultSites +
                '}';
    }


}
