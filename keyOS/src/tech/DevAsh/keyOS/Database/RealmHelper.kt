package tech.DevAsh.KeyOS.Database

import android.content.Context
import io.realm.Realm
import tech.DevAsh.keyOS.Database.User

object RealmHelper {
    fun init(context: Context){
        Realm.init(context)
    }

    fun updateUser(user: User){
        Realm.getDefaultInstance().executeTransactionAsync{
            it.delete(User::class.java)
            it.insertOrUpdate(user)
            UserContext.user = Realm.getDefaultInstance()
                .copyFromRealm(Realm.getDefaultInstance().where(User::class.java).findFirst()!!)

        }

    }
}