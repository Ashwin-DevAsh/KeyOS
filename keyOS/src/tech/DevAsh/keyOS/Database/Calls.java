package tech.DevAsh.keyOS.Database;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import io.realm.RealmList;
import io.realm.RealmObject;
import java.util.ArrayList;

@Keep
public class Calls extends RealmObject {
    public Calls(
            Boolean allowCalls,
            Boolean allowIncoming,
            Boolean allowOutgoing,
            Boolean whitelistCalls,
            Boolean blackListCalls,
            Boolean automaticWhitelist,
            RealmList<Contact> whiteListContacts,
            RealmList<Contact> blacklistContacts) {
        this.allowCalls = allowCalls;
        this.allowIncoming = allowIncoming;
        this.allowOutgoing = allowOutgoing;
        this.whitelistCalls = whitelistCalls;
        this.blackListCalls = blackListCalls;
        this.whiteListContacts = whiteListContacts;
        this.automaticWhitelist = automaticWhitelist;
        this.blacklistContacts = blacklistContacts;
    }

    public Calls(){}

    @SerializedName("allowCalls")
    public Boolean allowCalls = true;

    @SerializedName("allowIncoming")
    public Boolean allowIncoming = true;

    @SerializedName("allowOutgoing")
    public Boolean allowOutgoing = true;

    @SerializedName("whitelistCalls")
    public Boolean whitelistCalls = false;

    @SerializedName("blackListCalls")
    public Boolean blackListCalls = false;

    @SerializedName("automaticWhitelist")
    public Boolean automaticWhitelist = false;

    @SerializedName("whiteListContacts")
    public RealmList<Contact> whiteListContacts = new RealmList();

    @SerializedName("blacklistContacts")
    public RealmList<Contact> blacklistContacts = new RealmList();

    @Override
    public String toString() {
        return "Calls{" +
                "allowCalls=" + allowCalls +
                ", allowIncoming=" + allowIncoming +
                ", allowOutgoing=" + allowOutgoing +
                ", whitelistCalls=" + whitelistCalls +
                ", blackListCalls=" + blackListCalls +
                ", automaticWhitelist=" + automaticWhitelist +
                ", whiteListContacts=" + whiteListContacts +
                ", blacklistContacts=" + blacklistContacts +
                '}';
    }
}
