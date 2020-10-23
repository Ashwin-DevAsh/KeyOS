package tech.DevAsh.KeyOS.Config

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import com.android.launcher3.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
        onTextChangeListener(name, nameLayout, "name")
        onTextChangeListener(number, numberLayout, "number")

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

        numberLayout.setOnClickListener{
            numberLayout.error=null
        }
    }

    private fun onTextChangeListener(editText: TextInputEditText, layout: TextInputLayout, type: String){
        editText.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                when(type){
                    "name"->this@ContactsInput.nameError()
                    "number"->this@ContactsInput.numberError()
                }
            }

        })
        editText.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus && editText.text.isNullOrEmpty()){
                   layout.error=null
            }
        }
    }

    fun nameError(){
        if(name.text.toString().length<3){
            nameLayout.error="Invalid name"
        }else{
            nameLayout.error=null
        }
    }

    fun numberError(){
        if(number.text.toString().length<3){
            numberLayout.error="Invalid number"
        }else{
            numberLayout.error=null
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
       }
    }

    private fun isValid():Boolean{
        if(name.text!!.length>=3  && number.text!!.length>=3 ){
            return true
        }
//        if(name.text!!.length<3 && !name.text.isNullOrEmpty()){
//            nameLayout.error="Invalid name"
//        }
//        if(number.text!!.length<3 && !number.text.isNullOrEmpty()){
//            numberLayout.error="Invalid number"
//        }
        return false
    }
}