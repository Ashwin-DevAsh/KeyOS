package tech.DevAsh.KeyOS.Config

import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.launcher3.R
import tech.DevAsh.KeyOS.Config.Adapters.PhoneBookAdapter
import tech.DevAsh.KeyOS.Config.ContactList.Companion.isBlackList
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Helpers.ContactHelper
import tech.DevAsh.KeyOS.Helpers.PermissionsHelper
import io.realm.RealmList
import kotlinx.android.synthetic.keyOS.activity_activity_list.cancel
import kotlinx.android.synthetic.keyOS.activity_activity_list.done
import kotlinx.android.synthetic.keyOS.activity_activity_list.searchView
import kotlinx.android.synthetic.keyOS.activity_phonebook.*
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.Contact

import java.util.*


class PhoneBook : AppCompatActivity() {

    companion object{
        var allContacts : ArrayList<Contact>?=null
        var phoneBookAdapter : PhoneBookAdapter?=null
    }

    private var loadContact:LoadContact= LoadContact(this,object:CallBack{
        override fun onComplete(contacts: ArrayList<Contact>) {
            updateView( contacts)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmHelper.init(applicationContext)
        setContentView(R.layout.activity_phonebook)
        onClick()
        handelSearch()
        if(phoneBookAdapter==null)
             getContact()
        else
           loadFromCache()
    }

    private fun handelSearch(){
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            var handler=Handler()
            var runnable:Runnable?=null
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handler.removeCallbacksAndMessages(true)
                handler.removeCallbacks(runnable!!)
                val query = newText.toString().toLowerCase(Locale.ROOT)
                runnable = Runnable{
                    phoneBookAdapter?.items?.clear()
                    for(i in allContacts!!){
                        if(i.name.toLowerCase(Locale.ROOT)
                                .contains(query) || (i.name=="" && i.number=="")){
                            phoneBookAdapter?.items?.add(i)
                        }
                    }
                    phoneBookAdapter?.notifyDataSetChanged()
                }
                handler.postDelayed(runnable!!,500)
                return true
            }

        })
    }



    private fun loadFromCache(){
        Handler().postDelayed({
            contactsContainer.layoutManager = LinearLayoutManager(this)
            contactsContainer.adapter=phoneBookAdapter
            phoneBookAdapter?.updateList(allContacts!!)
            loadingScreen.visibility=View.INVISIBLE
            mainContent.visibility=View.VISIBLE
        },1000)
    }

    private fun onClick(){
        done.setOnClickListener {
            saveData()
        }

        cancel.setOnClickListener {
            super.onBackPressed()
        }

        back.setOnClickListener {
            super.onBackPressed()
        }
    }


    private fun saveData(){
        val newContacts = RealmList<Contact>()
        ContactList.contactList.addAll(1, ArrayList<Contact>(phoneBookAdapter!!.selectedContact))
        for(i in  ContactList.contactList){
           if(!newContacts.contains(i) && i.number!="")
               newContacts.add(i)
        }
        if(isBlackList){
            UserContext.user!!.calls.blacklistContacts = newContacts
        }else{
            UserContext.user!!.calls.whiteListContacts= newContacts
        }
        phoneBookAdapter!!.selectedContact= ArrayList<Contact>()
        ContactList.contactListAdapter?.updateList( ContactList.contactList)
        RealmHelper.updateUser(UserContext.user!!)
        finish()
    }


    fun updateView(contacts: ArrayList<Contact>){
        Handler().postDelayed({
            phoneBookAdapter = PhoneBookAdapter(
                ArrayList(contacts),
                this,
                if(isBlackList) "Select which contacts you want to add\n in blacklist"
                else  "Select which contacts you want to add\n in whitelist"
            )
            contactsContainer.layoutManager = LinearLayoutManager(this)
            contactsContainer.adapter=phoneBookAdapter
            loadingScreen.visibility=View.INVISIBLE
            mainContent.visibility=View.VISIBLE
        },1000)
    }


    private fun getContact(){
        Handler().post{
            if(PermissionsHelper.checkRuntimePermission(this,android.Manifest.permission.READ_CONTACTS)){
                loadContact.execute()
            }else{
                PermissionsHelper.getRuntimePermission(this, arrayOf(android.Manifest.permission.READ_CONTACTS),0)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getContact()
            }else{
                onBackPressed()
            }
        }
    }
}

class LoadContact(val activity: AppCompatActivity, private val callBack: CallBack):AsyncTask<Any,Any,Any>(){

    override fun doInBackground(vararg params: Any?): Any {
        PhoneBook.allContacts =  ContactHelper.instance.fetchContactsCProviderClient(activity)
        PhoneBook.allContacts!!.add(0,Contact("",""))
        return true
    }

    override fun onPostExecute(result: Any?) {
        callBack.onComplete(ArrayList(PhoneBook.allContacts!!))
        super.onPostExecute(result)
    }
}

interface CallBack{
    fun onComplete(contacts:ArrayList<Contact>)
}


