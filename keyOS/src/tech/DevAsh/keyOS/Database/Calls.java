package tech.DevAsh.keyOS.Database;

import io.realm.RealmList;
import io.realm.RealmObject;
import java.util.ArrayList;

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

    public Boolean allowCalls = true;
    public Boolean allowIncoming = true;
    public Boolean allowOutgoing = true;
    public Boolean whitelistCalls = false;
    public Boolean blackListCalls = false;
    public Boolean automaticWhitelist = false;
    public RealmList<Contact> whiteListContacts = new RealmList();
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
