package tech.DevAsh.KeyOS.Helpers

import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.provider.ContactsContract
import tech.DevAsh.keyOS.Database.Contact

import java.util.*
import kotlin.collections.ArrayList


internal class ContactHelper private constructor() {

     val mColors = arrayOf(
     "0979F2","F69705",  "5BD569", "6566C8", "E91E63", "9C27B0", "03A9F4", "4CAF50", "0979F2",
        "F44336"
    )

    fun getRandomMaterialColor(): Int {
        return Color.parseColor("#" + mColors[Random().nextInt(mColors.size)])
    }

     fun fetchContactsCProviderClient(context: Context): ArrayList<Contact> {
         val mContactList= ArrayList<Contact>()

         val projection = arrayOf(
             ContactsContract.Contacts.DISPLAY_NAME,
             ContactsContract.CommonDataKinds.Phone.NUMBER
         )
         val cr: ContentResolver = context.contentResolver
         cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
             projection,
             null,
             null,
             ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")?.use { cursor ->
             val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
             val numberIndex =
                 cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
             var name: String?
             var number: String?
             while (cursor.moveToNext()) {
                 name = cursor.getString(nameIndex)
                 number = reformatNumber(cursor.getString(numberIndex))
                 val contact = Contact(name, number)
                 if(!mContactList.contains(contact)){
                     mContactList.add(contact)
                 }
             }
         }

        return mContactList
    }

    private fun reformatNumber(number: String):String{
        var reformatNumber:String = number.replace(" ","").replace("-","")
        println(reformatNumber)
        return if(reformatNumber.startsWith("+")){
            reformatNumber
        }else{
            "+91$reformatNumber"
        }
    }

    companion object {
        val instance = ContactHelper()
    }
}