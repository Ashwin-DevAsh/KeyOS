package tech.DevAsh.keyOS.Database

import io.realm.DynamicRealm
import io.realm.RealmMigration


class RealmMigrations : RealmMigration{
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema

        if (oldVersion == 1L) {
            val userSchema = schema.get("BasicSettings")
            userSchema!!.addField("isDisableCamera", Boolean::class.javaPrimitiveType).transform {
                it.setBoolean("isDisableCamera",false)
            }

        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }


}