package tech.DevAsh.keyOS.Database;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

@Keep
public class WebFilterDB extends RealmObject {
    @SerializedName("isEnabled")
    public boolean isEnabled = false;
    @SerializedName("isWhitelistEnabled")
    public boolean isWhitelistEnabled = false;
    @SerializedName("isBlacklistEnabled")
    public boolean isBlacklistEnabled = false;
    @SerializedName("whitelistWebsites")
    @Required
    public RealmList<String> whitelistWebsites = new RealmList();
    @SerializedName("blacklistWebsites")
    @Required
    public RealmList<String> blacklistWebsites = new RealmList();
    @SerializedName("shouldBlockAdultSites")
    public boolean shouldBlockAdultSites = false;

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

    public WebFilterDB(Boolean isEnabled, Boolean isWhitelistEnabled,
            Boolean isBlacklistEnabled, RealmList<String> whitelistWebsites,
            RealmList<String> blacklistWebsites, Boolean shouldBlockAdultSites) {
        this.isEnabled = isEnabled;
        this.isWhitelistEnabled = isWhitelistEnabled;
        this.isBlacklistEnabled = isBlacklistEnabled;
        this.whitelistWebsites = whitelistWebsites;
        this.blacklistWebsites = blacklistWebsites;
        this.shouldBlockAdultSites = shouldBlockAdultSites;
    }

    public WebFilterDB(){}
}
