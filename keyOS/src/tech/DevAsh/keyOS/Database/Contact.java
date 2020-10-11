package tech.DevAsh.keyOS.Database;

import io.realm.RealmObject;
import java.util.Objects;

public class Contact extends RealmObject {
    public String name;
    public String number;

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public Contact(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return Objects.equals(number, contact.number);
    }
}
