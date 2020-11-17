package tech.DevAsh.keyOS.Database

import android.content.Context
import android.widget.Toast
import io.realm.*


class RealmMigrations(val context: Context) : RealmMigration{


    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema
        var oldVersion = oldVersion


        if(oldVersion== 1L){
            val userSchema = schema.get("BasicSettings")
            userSchema!!
                    .addField("isDisableCamera", Boolean::class.javaPrimitiveType)
                    .transform { it.setBoolean("isDisableCamera",false) }
            oldVersion++

        }
        if(oldVersion==2L){
            val webFilter = schema.create("WebFilterDB")
            webFilter
                    .addField("isEnabled",Boolean::class.javaObjectType)
                    .transform { it.setBoolean("isEnabled",false) }
            webFilter
                    .addField("isWhitelistEnabled",Boolean::class.javaObjectType)
                    .transform { it.setBoolean("isWhitelistEnabled",false) }
            webFilter
                    .addField("isBlacklistEnabled",Boolean::class.javaObjectType)
                    .transform { it.setBoolean("isBlacklistEnabled",false) }

            webFilter
                    .addRealmListField("whitelistWebsites", String::class.javaObjectType)
                    .transform { it.setList("whitelistWebsites", RealmList<String>()) }
            webFilter
                    .addRealmListField("blacklistWebsites",String::class.javaObjectType)
                    .transform { it.setList("blacklistWebsites",RealmList<String>()) }

            webFilter
                    .addField("shouldBlockAdultSites",Boolean::class.javaObjectType)
                    .transform { it.setBoolean("shouldBlockAdultSites",false) }

            val user = schema.get("User")
            user!!.addRealmObjectField("webFilter",webFilter)

            oldVersion++
        }
        if(oldVersion==3L){
            val webFilter = schema.get("WebFilterDB")!!
            webFilter.setRequired("isEnabled",true)
            webFilter.setRequired("isWhitelistEnabled",true)
            webFilter.setRequired("isBlacklistEnabled",true)
            webFilter.setRequired("whitelistWebsites", true)
            webFilter.setRequired("blacklistWebsites",true)
            webFilter.setRequired("shouldBlockAdultSites",true)
            oldVersion++
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