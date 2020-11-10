package tech.DevAsh.keyOS.Database;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;
import io.realm.RealmObject;
import java.util.Objects;

@Keep
public class Contact extends RealmObject {

    @SerializedName("name")
    public String name;

    @SerializedName("number")
    public String number;

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public Contact(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return Objects.equals(number, contact.number);
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
