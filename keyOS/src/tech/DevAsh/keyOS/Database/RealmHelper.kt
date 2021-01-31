package tech.DevAsh.KeyOS.Database

import android.content.Context
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import tech.DevAsh.keyOS.Database.RealmMigrations
import tech.DevAsh.keyOS.Database.User

object RealmHelper {
    fun init(context: Context){
        Realm.init(context)
        val mConfiguration = RealmConfiguration.Builder()
                .name("RealmData.realm")
                .schemaVersion(9)
                .migration(RealmMigrations(context))
                .build()
        Realm.setDefaultConfiguration(mConfiguration)
    }

    fun updateUser(user: User){
        Realm.getDefaultInstance().executeTransaction{
            it.delete(User::class.java)
            it.insertOrUpdate(user)
            User.user = Realm.getDefaultInstance()
                .copyFromRealm(Realm.getDefaultInstance().where(User::class.java).findFirst()!!)

        }

    }
}