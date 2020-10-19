package tech.DevAsh.KeyOS.Config

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.android.launcher3.R
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.KeyOS.Helpers.UiHelper
import io.realm.RealmList
import kotlinx.android.synthetic.keyOS.activity_contacts_input.*
import tech.DevAsh.KeyOS.Database.*
import tech.DevAsh.keyOS.Database.Contact

class ContactsInput : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts_input)
        onClick()

    }

    fun onClick(){
        cancel.setOnClickListener {
            super.onBackPressed()
        }

        done.setOnClickListener {
            save()
        }

        back.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun save(){
       if(isValid()){
           ContactList.contactList.add(1, Contact(name.text.toString(), number.text.toString()))
           val newContacts = RealmList<Contact>()
           for(i in  ContactList.contactList){
               if(!newContacts.contains(i) && i.number!="")
                   newContacts.add(i)
           }

           if(ContactList.isBlackList){
               UserContext.user!!.calls.blacklistContacts = newContacts
           }else{
               UserContext.user!!.calls.whiteListContacts= newContacts
           }
           ContactList.contactListAdapter?.updateList(ContactList.contactList)
           RealmHelper.updateUser(UserContext.user!!)
           UiHelper.hideKeyboard(this)
           Handler().postDelayed({
               finish()
           },500)
       } else{
           AlertHelper.showError("Invalid credentials",this)
       }
    }

    private fun isValid():Boolean{
        if(name.text.isEmpty() || number.text.length<5 ){
            return false
        }
        return true
    }
}